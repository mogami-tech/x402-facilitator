package tech.mogami.facilitator.provider.outbox.event;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.RequestCommonData;
import tech.mogami.commons.api.facilitator.settle.SettleRequest;
import tech.mogami.commons.api.facilitator.settle.SettleResponse;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.domain.payment.Payment;
import tech.mogami.facilitator.domain.payment.PaymentStep;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEventType;
import tech.mogami.facilitator.provider.outbox.handler.OutboxEventHandler;
import tech.mogami.facilitator.provider.outbox.handler.OutboxEventHandlerResult;
import tech.mogami.facilitator.repository.AddressRepository;
import tech.mogami.facilitator.repository.PaymentRepository;
import tech.mogami.facilitator.repository.PaymentStepRepository;
import tech.mogami.facilitator.service.data.ParticipantService;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static tech.mogami.commons.payment.PaymentStatus.COMPLETED;
import static tech.mogami.commons.payment.PaymentStatus.FAILED;
import static tech.mogami.facilitator.domain.payment.PaymentStepType.SETTLE;
import static tech.mogami.facilitator.domain.payment.PaymentStepType.VERIFY;
import static tech.mogami.facilitator.domain.platform.outbox.OutboxEventType.NEW_PAYMENT_STEP;

/**
 * Handler for new payment step outbox events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class NewPaymentStepHandler implements OutboxEventHandler<NewPaymentStepMessage> {

    /** Payment repository. */
    private final PaymentRepository paymentRepository;

    /** Payment step repository. */
    private final PaymentStepRepository paymentStepRepository;

    /** Address repository. */
    private final AddressRepository addressRepository;

    /** Participant service. */
    private final ParticipantService participantService;

    @Override
    public OutboxEventType supports() {
        return NEW_PAYMENT_STEP;
    }

    @Override
    public OutboxEventHandlerResult handle(final NewPaymentStepMessage payload) {
        log.info("Handling new payment step for paymentId: {}", payload.paymentId());

        // We retrieve the payment and add the payment step ============================================================
        Payment payment;
        try {
            payment = getOrCreatePayment(payload.paymentId());
            log.info("New payment step {} with id: {}", payload.paymentId(), payment.getId());

            final PaymentStep paymentStep = PaymentStep.builder()
                    .paymentStepId(UUID.randomUUID().toString())
                    .payment(payment)
                    .paymentStepType(payload.paymentStepType())
                    .requestPayload(payload.requestPayload())
                    .responsePayload(payload.responsePayload())
                    .errorCode(payload.errorCode())
                    .errorMessage(payload.errorMessage())
                    .build();
            payment.addStep(paymentStep);
            log.info("Payment step persisted for payment {} with paymentStepId {}", payload.paymentId(), paymentStep.getPaymentStepId());
            paymentStepRepository.save(paymentStep);

        } catch (Exception e) {
            log.error("Error handling NEW_PAYMENT_STEP: {}", e.getMessage(), e);
            return OutboxEventHandlerResult.error(e.getMessage());
        }

        // We determine the steps to treat =============================================================================
        getStepsToProcess(payment.getSteps())
                .forEach(step -> {
                    RequestCommonData request = null;
                    SettleResponse settleResponse = null;

                    // We retrieve the JSON data =======================================================================
                    if (step.getPaymentStepType() == VERIFY) {
                        request = JsonUtil.fromJson(step.getRequestPayload(), VerifyRequest.class);
                    }
                    if (step.getPaymentStepType() == SETTLE) {
                        request = JsonUtil.fromJson(step.getRequestPayload(), SettleRequest.class);
                        settleResponse = JsonUtil.fromJson(step.getResponsePayload(), SettleResponse.class);
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
                });
        return OutboxEventHandlerResult.ok();
    }

    /**
     * Retrieves an existing payment by nonce or creates a new one if it doesn't exist.
     *
     * @param paymentId the payment nonce
     * @return the existing or newly created Payment
     */
    public Payment getOrCreatePayment(final @NonNull String paymentId) {
        log.info("Creating or retrieving payment with paymentId: {}", paymentId);
        // We try to find the payment.
        return paymentRepository.findByPaymentId(paymentId)
                // If not found, we create it.
                .orElseGet(() -> {
                    try {
                        return paymentRepository.saveAndFlush(Payment.builder()
                                .paymentId(paymentId)
                                .build());
                    } catch (DataIntegrityViolationException e) {
                        // Seems someone else created it in between, so we try again to find it.
                        return paymentRepository.findByPaymentId(paymentId)
                                .orElseThrow(() -> new IllegalStateException("Payment should exist"));
                    }
                });
    }

    /**
     * Determines the payment steps to process for a given list of steps.
     *
     * @param steps the list of payment steps
     * @return the list of payment steps to process
     */
    private List<PaymentStep> getStepsToProcess(final List<PaymentStep> steps) {
        // Do we have steps of SETTLE type? and is there a successful one among them? ==================================
        LinkedList<PaymentStep> verifySteps = steps.stream()
                .filter(step -> step.getPaymentStepType() == VERIFY)
                .collect(Collectors.toCollection(LinkedList::new));
        Optional<PaymentStep> successfulVerifyStep = verifySteps.stream()
                .filter(PaymentStep::hasNoError)
                .findFirst();
        LinkedList<PaymentStep> settleSteps = steps.stream()
                .filter(step -> step.getPaymentStepType() == SETTLE)
                .collect(Collectors.toCollection(LinkedList::new));
        Optional<PaymentStep> successfulSettleStep = settleSteps.stream()
                .filter(PaymentStep::hasNoError)
                .findFirst();

        // Choose the steps to process =================================================================================
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
    }

}
