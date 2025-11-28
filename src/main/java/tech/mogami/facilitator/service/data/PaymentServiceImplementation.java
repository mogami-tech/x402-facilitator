package tech.mogami.facilitator.service.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import tech.mogami.commons.api.facilitator.RequestCommonData;
import tech.mogami.commons.api.facilitator.settle.SettleRequest;
import tech.mogami.commons.api.facilitator.settle.SettleResponse;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.domain.payment.Payment;
import tech.mogami.facilitator.domain.payment.PaymentStep;
import tech.mogami.facilitator.dto.payment.PaymentDto;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;
import tech.mogami.facilitator.repository.AddressRepository;
import tech.mogami.facilitator.repository.PaymentRepository;
import tech.mogami.facilitator.util.base.BaseService;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

import static tech.mogami.commons.payment.PaymentStatus.COMPLETED;
import static tech.mogami.commons.payment.PaymentStatus.FAILED;
import static tech.mogami.facilitator.domain.payment.PaymentStepType.SETTLE;
import static tech.mogami.facilitator.domain.payment.PaymentStepType.VERIFY;

/**
 * {@link PaymentService} implementation.
 */
@Slf4j
@Service
@Validated
@Transactional
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class PaymentServiceImplementation extends BaseService implements PaymentService {

    /** Address repository. */
    private final AddressRepository addressRepository;

    /** Payment repository. */
    private final PaymentRepository paymentRepository;

    /** Participant service. */
    private final ParticipantService participantService;

    @Override
    public void logPaymentStep(final PaymentStepDto paymentStep) {
        log.info("Logging payment step: {}", paymentStep);

        // We save the information in database =========================================================================
        // We get or create the payment, and we add the step
        final Payment payment = getOrCreatePayment(paymentStep.nonce());
        payment.addStep(PaymentStep.builder()
                .paymentStepId(UUID.randomUUID().toString())
                .payment(payment)
                .paymentStepType(paymentStep.paymentStepType())
                .requestPayload(paymentStep.requestPayload())
                .responsePayload(paymentStep.responsePayload())
                .errorCode(paymentStep.errorCode())
                .errorMessage(paymentStep.errorMessage())
                .build());
        paymentRepository.save(payment);
        log.info("Payment step {} saved", paymentStep);

        // TODO Remove this.
        paymentRepository.findByPaymentId(payment.getPaymentId())
                .stream()
                .peek(payment1 -> System.out.println("=> Displaying payment: " + payment1.getPaymentId()))
                .map(Payment::getSteps)
                .forEach(steps1 -> steps1.forEach(step1 -> {
                    System.out.println("==> step in payment: " + step1.getId());
                    System.out.println("==> step in payment: " + step1.getPaymentStepId());
                    System.out.println("==> step in payment: " + step1.getRequestPayload());
                    System.out.println();
                }));

        payment.getSteps().forEach(step -> {
            System.out.println("=> Step :" + step.informationScore());
            System.out.println("=> Step score:" + step.informationScore());
            System.out.println("=> Step date:" + step.getCreatedAt());
        });

        // We try to find the most useful step by following a specific order ===========================================
        // The order is:
        // - The latest successful payment with step = SETTLE
        // - The latest failed payment with step = SETTLE
        // - The latest successful payment with step = VERIFY
        // - The latest failed payment with step = VERIFY
        Optional<PaymentStep> usefulPaymentStep = payment.getSteps().stream()
                .max(Comparator.comparingInt(PaymentStep::informationScore)
                        .thenComparing(PaymentStep::getCreatedAt)
                );

        // With the one found, we are going to retrieve the data =======================================================
        usefulPaymentStep.ifPresentOrElse(step -> {
            log.info("Most useful payment step found: {}", step.getPaymentStepId());
            RequestCommonData request = null;
            SettleResponse settleResponse = null;

            // We retrieve the JSON data ===============================================================================
            if (step.getPaymentStepType() == VERIFY) {
                try {
                    request = JsonUtil.fromJson(step.getRequestPayload(), VerifyRequest.class);
                } catch (IllegalArgumentException e) {
                    log.error("Failed to parse verify request payload for payment step: {}", step.getPaymentStepId(), e);
                }
            }
            if (step.getPaymentStepType() == SETTLE) {
                try {
                    request = JsonUtil.fromJson(step.getRequestPayload(), SettleRequest.class);
                } catch (IllegalArgumentException e) {
                    log.error("Failed to parse settle request payload for payment step: {}", step.getPaymentStepId(), e);
                }
                try {
                    settleResponse = JsonUtil.fromJson(step.getResponsePayload(), SettleResponse.class);
                } catch (IllegalArgumentException e) {
                    log.error("Failed to parse settle response payload for payment step: {}", step.getPaymentStepId(), e);
                }
            }

            // We update the payment ===================================================================================
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

            // Searching for a settle response to set the status =======================================================
            if (settleResponse != null) {
                if (settleResponse.success()) {
                    payment.setStatus(COMPLETED);
                } else {
                    payment.setStatus(FAILED);
                }
            }

            // Save the payment ========================================================================================
            paymentRepository.save(payment);
            log.info("Payment {} updated", payment.getPaymentId());

        }, () -> log.error("No useful payment step found for payment: {}", payment.getPaymentId()));

    }

    @Override
    public Optional<PaymentDto> searchPaymentById(final String paymentId) {
        log.debug("Retrieving payment by ID: {}", paymentId);
        if (StringUtils.isBlank(paymentId)) {
            return Optional.empty();
        } else {
            System.out.println("=> Displaying " + paymentRepository.findByPaymentId(paymentId));
            return paymentRepository.findByPaymentId(paymentId)
                    .map(PAYMENT_MAPPER::toDto);
        }
    }

    /**
     * Creates or retrieves a payment based on the provided nonce.
     *
     * @param nonce the nonce associated with the payment
     * @return the created or retrieved PaymentStep
     */
    private Payment getOrCreatePayment(final String nonce) {
        log.info("Creating or retrieving payment with nonce: {}", nonce);
        // We try to find the payment.
        return paymentRepository.findByPaymentId(nonce)
                // If not found, we create it.
                .orElseGet(() -> {
                    try {
                        return paymentRepository.save(Payment.builder()
                                .paymentId(nonce)
                                .build());
                    } catch (DataIntegrityViolationException e) {
                        // Seems someone else created it in between, so we try again to find it.
                        return paymentRepository.findByPaymentId(nonce)
                                .orElseThrow(() -> new IllegalStateException("Payment should exist"));
                    }
                });
    }

}
