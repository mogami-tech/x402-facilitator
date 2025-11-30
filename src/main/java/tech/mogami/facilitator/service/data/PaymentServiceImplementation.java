package tech.mogami.facilitator.service.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import tech.mogami.facilitator.domain.payment.Payment;
import tech.mogami.facilitator.dto.payment.PaymentDto;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;
import tech.mogami.facilitator.repository.AddressRepository;
import tech.mogami.facilitator.repository.PaymentRepository;
import tech.mogami.facilitator.util.base.BaseService;

import java.util.Optional;

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

    /** Participant step service. */
    private final PaymentServiceAsyncImplementation paymentStepService;

    @Override
    public void logPaymentStep(final PaymentStepDto paymentStep) {
        log.info("Logging payment step: {}", paymentStep);
        final Payment payment = getOrCreatePayment(paymentStep.nonce());
        paymentStepService.logPaymentStep(payment.getPaymentId(), paymentStep);
    }

    @Override
    public Optional<PaymentDto> searchPaymentById(final String paymentId) {
        log.debug("Retrieving payment by paymentId: {}", paymentId);
        return Optional.ofNullable(StringUtils.trimToNull(paymentId))
                .flatMap(paymentRepository::findByPaymentId)
                .map(PAYMENT_MAPPER::toDto);
    }

    /**
     * Creates or retrieves a payment based on the provided nonce.
     *
     * @param nonce the nonce associated with the payment
     * @return the created or retrieved PaymentStep
     */
    private Payment getOrCreatePayment(final String nonce) {
        log.info("Creating or retrieving payment with paymentId: {}", nonce);
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
