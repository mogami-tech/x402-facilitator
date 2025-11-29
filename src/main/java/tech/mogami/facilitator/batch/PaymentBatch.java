package tech.mogami.facilitator.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.mogami.commons.api.facilitator.RequestCommonData;
import tech.mogami.commons.api.facilitator.settle.SettleRequest;
import tech.mogami.commons.api.facilitator.settle.SettleResponse;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.domain.payment.Payment;
import tech.mogami.facilitator.domain.payment.PaymentStep;
import tech.mogami.facilitator.repository.AddressRepository;
import tech.mogami.facilitator.repository.PaymentRepository;
import tech.mogami.facilitator.service.data.ParticipantService;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

import static tech.mogami.commons.payment.PaymentStatus.COMPLETED;
import static tech.mogami.commons.payment.PaymentStatus.FAILED;
import static tech.mogami.facilitator.domain.payment.PaymentStepType.SETTLE;
import static tech.mogami.facilitator.domain.payment.PaymentStepType.VERIFY;

/**
 * Batch process for updating payments.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:DesignForExtension")
public class PaymentBatch {

    /** Start delay in milliseconds (1 000 ms = 1 second). */
    private static final int START_DELAY_IN_MILLISECONDS = 1_000;

    /** Delay between two calls to process requests (1 000 ms = 1 second). */
    private static final int DELAY_BETWEEN_TWO_PROCESS_IN_MILLISECONDS = 1_000;

    /** Default batch size. */
    private static final int DEFAULT_BATCH_SIZE = 10;

    /** Address repository. */
    private final AddressRepository addressRepository;

    /** Payment repository. */
    private final PaymentRepository paymentRepository;

    /** Participant service. */
    private final ParticipantService participantService;

    /**
     * Scheduled method to update payments.
     * This method runs periodically to process payments that need updating.
     */
    @Scheduled(initialDelay = START_DELAY_IN_MILLISECONDS, fixedDelay = DELAY_BETWEEN_TWO_PROCESS_IN_MILLISECONDS)
    @Transactional
    public void updatePayments() {
        paymentRepository.paymentsToUpdate(PageRequest.of(0, DEFAULT_BATCH_SIZE))
                .stream()
                // We retrieve the payment =============================================================================
                .map(paymentRepository::findByPaymentId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                // We determine the steps to process ===================================================================
                .map(payment -> {
                    // Do we have steps of SETTLE type? and is there a successful one among them? ======================
                    LinkedList<PaymentStep> verifySteps = payment.getSteps().stream()
                            .filter(step -> step.getPaymentStepType() == VERIFY)
                            .collect(Collectors.toCollection(LinkedList::new));
                    Optional<PaymentStep> successfulVerifyStep = verifySteps.stream()
                            .filter(PaymentStep::hasNoError)
                            .findFirst();
                    LinkedList<PaymentStep> settleSteps = payment.getSteps().stream()
                            .filter(step -> step.getPaymentStepType() == SETTLE)
                            .collect(Collectors.toCollection(LinkedList::new));
                    Optional<PaymentStep> successfulSettleStep = settleSteps.stream()
                            .filter(PaymentStep::hasNoError)
                            .findFirst();

                    // Choose the steps to process =====================================================================
                    LinkedList<PaymentStep> stepsToProcess = new LinkedList<>();
                    if (settleSteps.isEmpty()) {
                        // No SETTLE steps, we treat VERIFY steps.
                        successfulVerifyStep.ifPresentOrElse(
                                // We have a successful VERIFY step, we treat only it.
                                stepsToProcess::add,
                                // No successful VERIFY step, we treat all of them.
                                () -> stepsToProcess.addAll(verifySteps)
                        );
                    } else {
                        // We have SETTLE steps, we treat them.
                        successfulSettleStep.ifPresentOrElse(
                                // We have a successful SETTLE step, we treat only it.
                                stepsToProcess::add,
                                // No successful SETTLE step, we treat all of them.
                                () -> stepsToProcess.addAll(settleSteps)
                        );
                    }
                    return stepsToProcess;
                })
                // We process the steps ================================================================================
                .flatMap(Collection::stream)
                .forEach(step -> {
                    Payment payment = step.getPayment();
                    RequestCommonData request = null;
                    SettleResponse settleResponse = null;

                    // We retrieve the JSON data =======================================================================
                    if (step.getPaymentStepType() == VERIFY) {
                        try {
                            request = JsonUtil.fromJson(step.getRequestPayload(), VerifyRequest.class);
                        } catch (IllegalArgumentException e) {
                            log.debug("Failed to parse verify request for paymentId: {}", step.getPaymentStepId(), e);
                        }
                    }
                    if (step.getPaymentStepType() == SETTLE) {
                        try {
                            request = JsonUtil.fromJson(step.getRequestPayload(), SettleRequest.class);
                        } catch (IllegalArgumentException e) {
                            log.debug("Failed to parse settle request payload for paymentId: {}", step.getPaymentStepId(), e);
                        }
                        try {
                            settleResponse = JsonUtil.fromJson(step.getResponsePayload(), SettleResponse.class);
                        } catch (IllegalArgumentException e) {
                            log.debug("Failed to parse settle response payload  for paymentId: {}", step.getPaymentStepId(), e);
                        }
                    }

                    // We update the payment ===========================================================================
                    if (request != null) {
                        request.getFromAddress().ifPresent(addressAsString -> {
                            participantService.getOrCreateAddress(addressAsString);
                            addressRepository.findByAddress(addressAsString).ifPresent(payment::setFrom);
                        });
                        request.getToAddress().ifPresent(addressAsString -> {
                            participantService.getOrCreateAddress(addressAsString);
                            addressRepository.findByAddress(addressAsString).ifPresent(payment::setTo);
                        });
                        request.getAssetAmount().ifPresent(payment::setAssetAmount);
                        request.getAssetContract().ifPresent(addressAsString -> {
                            participantService.getOrCreateAddress(addressAsString);
                            addressRepository.findByAddress(addressAsString).ifPresent(payment::setAssetContract);
                        });
                        request.getNetwork().ifPresent(networkValue -> payment.setNetworkName(networkValue.name()));
                    }
                    if (settleResponse != null) {
                        if (settleResponse.success()) {
                            payment.setStatus(COMPLETED);
                        } else {
                            payment.setStatus(FAILED);
                        }
                    }

                    // Save the payment ================================================================================
                    paymentRepository.save(payment);
                    log.info("Payment with paymentId {} updated", payment.getPaymentId());
                });
    }

}
