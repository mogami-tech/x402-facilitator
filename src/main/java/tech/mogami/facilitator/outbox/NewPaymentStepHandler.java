package tech.mogami.facilitator.outbox;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.mogami.commons.api.facilitator.RequestCommonData;
import tech.mogami.commons.api.facilitator.settle.SettleRequest;
import tech.mogami.commons.api.facilitator.settle.SettleResponse;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.domain.payment.Payment;
import tech.mogami.facilitator.domain.payment.PaymentStep;
import tech.mogami.facilitator.provider.outbox.domain.OutboxEventType;
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

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static tech.mogami.commons.payment.PaymentStatus.COMPLETED;
import static tech.mogami.commons.payment.PaymentStatus.FAILED;
import static tech.mogami.facilitator.domain.payment.PaymentStepType.SETTLE;
import static tech.mogami.facilitator.domain.payment.PaymentStepType.VERIFY;
import static tech.mogami.facilitator.provider.outbox.domain.OutboxEventType.NEW_PAYMENT_STEP;

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
    @Transactional(propagation = REQUIRES_NEW)
    public OutboxEventHandlerResult handle(final NewPaymentStepMessage message) {
        log.info("Handling new payment step for paymentId: {}", message.paymentId());

        // We retrieve the payment and add the payment step ============================================================
        try {
            Payment payment = getOrCreatePayment(message.paymentId());
            log.info("New payment step {} with id: {}", message.paymentId(), payment.getId());

            final PaymentStep paymentStep = PaymentStep.builder()
                    .paymentStepId(UUID.randomUUID().toString())
                    .payment(payment)
                    .paymentStepType(message.paymentStepType())
                    .requestPayload(message.requestPayload())
                    .responsePayload(message.responsePayload())
                    .errorCode(message.errorCode())
                    .errorMessage(message.errorMessage())
                    .build();
            payment.addStep(paymentStep);
            log.info("Payment step persisted for payment {} with paymentStepId {}", message.paymentId(), paymentStep.getPaymentStepId());
            paymentStepRepository.save(paymentStep);

            // We determine the steps to treat =========================================================================
            getStepsToProcess(payment.getSteps())
                    .forEach(step -> {
                        RequestCommonData request = null;
                        SettleResponse settleResponse = null;

                        // We retrieve the JSON data ===================================================================
                        if (step.getPaymentStepType() == VERIFY) {
                            try {
                                request = JsonUtil.fromJson(step.getRequestPayload(), VerifyRequest.class);
                            } catch (IllegalArgumentException e) {
                                log.warn("Unable to parse VerifyRequest from payment step id {}: {}", step.getPaymentStepId(), e.getMessage());
                            }
                        }
                        if (step.getPaymentStepType() == SETTLE) {
                            try {
                                request = JsonUtil.fromJson(step.getRequestPayload(), SettleRequest.class);
                            } catch (IllegalArgumentException e) {
                                log.warn("Unable to parse SettleRequest from payment step id {}: {}", step.getPaymentStepId(), e.getMessage());
                            }
                            try {
                                settleResponse = JsonUtil.fromJson(step.getResponsePayload(), SettleResponse.class);
                            } catch (IllegalArgumentException e) {
                                log.warn("Unable to parse SettleResponse from payment step id {}: {}", step.getPaymentStepId(), e.getMessage());
                            }
                        }

                        // We update the payment =======================================================================
                        if (request != null) {
                            request.getVersion().ifPresent(x402Version -> {
                                payment.setX402Version(x402Version.canonical());
                            });
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

        } catch (Exception e) {
            log.error("Error handling NEW_PAYMENT_STEP: {}", e.getMessage(), e);
            return OutboxEventHandlerResult.error(e.getMessage());
        }
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
        // Avoid errors ================================================================================================
        if (steps == null || steps.isEmpty()) {
            log.error("No steps to process - Abnormal situation");
            return List.of();
        }

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
                    // We have a successful VERIFY step, we treat only this one.
                    stepsToProcess::add,
                    // No successful VERIFY step, we treat all of them.
                    () -> stepsToProcess.addAll(verifySteps)
            );
        } else {
            // We have SETTLE steps, we treat them.
            successfulSettleStep.ifPresentOrElse(
                    // We have a successful SETTLE step, we treat only this one.
                    stepsToProcess::add,
                    // No successful SETTLE step, we treat all of them.
                    () -> stepsToProcess.addAll(settleSteps)
            );
        }
        return stepsToProcess;
    }

}
