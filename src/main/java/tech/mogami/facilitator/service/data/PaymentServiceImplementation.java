package tech.mogami.facilitator.service.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import tech.mogami.commons.api.facilitator.settle.SettleRequest;
import tech.mogami.commons.api.facilitator.settle.SettleResponse;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.constant.network.Networks;
import tech.mogami.commons.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.domain.payment.Payment;
import tech.mogami.facilitator.domain.payment.PaymentStep;
import tech.mogami.facilitator.dto.blockchain.AddressDto;
import tech.mogami.facilitator.dto.payment.PaymentDto;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;
import tech.mogami.facilitator.repository.AddressRepository;
import tech.mogami.facilitator.repository.PaymentRepository;
import tech.mogami.facilitator.util.base.BaseService;

import java.math.BigInteger;
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

        // We add the new payment step
        payment.addStep(PaymentStep.builder()
                .paymentStepId(UUID.randomUUID().toString())
                .payment(payment)
                .paymentStepType(paymentStep.paymentStepType())
                .requestPayload(paymentStep.requestPayload())
                .responsePayload(paymentStep.responsePayload())
                .errorCode(paymentStep.errorCode())
                .errorMessage(paymentStep.errorMessage())
                .build());

        // We try to find the most useful step by following a specific order ===========================================
        // - The latest successful payment with step = SETTLE
        Optional<PaymentStep> usefulPaymentStep = payment.getSteps().stream()
                .filter(step -> step.getPaymentStepType() == SETTLE)
                .filter(step -> step.getErrorCode() == null && step.getErrorMessage() == null)
                .reduce((first, second) -> second);
        // - The latest failed payment with step = SETTLE
        if (usefulPaymentStep.isEmpty()) {
            usefulPaymentStep = payment.getSteps().stream()
                    .filter(step -> step.getPaymentStepType() == SETTLE)
                    .filter(step -> step.getErrorCode() != null || step.getErrorMessage() != null)
                    .reduce((first, second) -> second);
        }
        // - The latest successful payment with step = VERIFY
        if (usefulPaymentStep.isEmpty()) {
            usefulPaymentStep = payment.getSteps().stream()
                    .filter(step -> step.getPaymentStepType() == VERIFY)
                    .filter(step -> step.getErrorCode() == null && step.getErrorMessage() == null)
                    .reduce((first, second) -> second);
        }
        // - The latest failed payment with step = VERIFY
        if (usefulPaymentStep.isEmpty()) {
            usefulPaymentStep = payment.getSteps().stream()
                    .filter(step -> step.getPaymentStepType() == VERIFY)
                    .filter(step -> step.getErrorCode() != null || step.getErrorMessage() != null)
                    .reduce((first, second) -> second);
        }

        // With the one found, we are going to retrieve the data =======================================================
        usefulPaymentStep.ifPresentOrElse(step -> {
            log.debug("Most useful payment step found: {}", step.getPaymentStepId());
            VerifyRequest verifyRequest = null;
            SettleRequest settleRequest = null;
            SettleResponse settleResponse = null;
            ExactSchemePayload payload = null;

            if (step.getPaymentStepType() == SETTLE) {
                try {
                    settleRequest = JsonUtil.fromJson(step.getRequestPayload(), SettleRequest.class);
                    payload = (ExactSchemePayload) settleRequest.paymentPayload().payload();
                } catch (IllegalArgumentException e) {
                    log.debug("Failed to parse settle request payload for payment step: {}", step.getPaymentStepId(), e);
                }
                try {
                    settleResponse = JsonUtil.fromJson(step.getResponsePayload(), SettleResponse.class);
                } catch (IllegalArgumentException e) {
                    log.debug("Failed to parse settle response payload for payment step: {}", step.getPaymentStepId(), e);
                }
            }
            if (step.getPaymentStepType() == VERIFY) {
                try {
                    verifyRequest = JsonUtil.fromJson(step.getRequestPayload(), VerifyRequest.class);
                    payload = (ExactSchemePayload) verifyRequest.paymentPayload().payload();
                } catch (IllegalArgumentException e) {
                    log.debug("Failed to parse verify request payload for payment step: {}", step.getPaymentStepId(), e);
                }
            }

            // We update the payment ===================================================================================
            AddressDto assetContract = null;
            Network network = null;
            if (settleRequest != null && settleRequest.paymentRequirements() != null) {
                assetContract = participantService.getOrCreateAddress(settleRequest.paymentRequirements().asset());
                network = Networks.findByName(settleRequest.paymentPayload().network()).orElse(null);
            } else if (verifyRequest != null && verifyRequest.paymentRequirements() != null) {
                assetContract = participantService.getOrCreateAddress(verifyRequest.paymentRequirements().asset());
                network = Networks.findByName(verifyRequest.paymentPayload().network()).orElse(null);
            }

            if (payload != null) {
                payment.setFrom(ADDRESS_MAPPER.toEntity(participantService.getOrCreateAddress(payload.authorization().from())));
                payment.setTo(ADDRESS_MAPPER.toEntity(participantService.getOrCreateAddress(payload.authorization().to())));
                payment.setAssetContract(ADDRESS_MAPPER.toEntity(assetContract));
                payment.setAssetAmount(new BigInteger(payload.authorization().value()));
                if (network != null) {
                    payment.setNetworkName(network.name());
                }
                // Searching for success in settle response to set the status
                if (settleResponse != null) {
                    if (settleResponse.success()) {
                        payment.setStatus(COMPLETED);
                    } else {
                        payment.setStatus(FAILED);
                    }
                }
            }
        }, () -> log.error("No useful payment step found for payment: {}", payment.getPaymentId()));

        // Save the payment ============================================================================================
        paymentRepository.save(payment);
        log.info("Payment {} updated", payment.getPaymentId());
    }

    @Override
    public Optional<PaymentDto> searchPaymentById(final String paymentId) {
        log.debug("Retrieving payment by ID: {}", paymentId);
        if (StringUtils.isBlank(paymentId)) {
            return Optional.empty();
        } else {
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
