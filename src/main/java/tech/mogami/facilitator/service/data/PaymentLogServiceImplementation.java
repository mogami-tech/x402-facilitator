package tech.mogami.facilitator.service.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.mogami.facilitator.domain.payment.Payment;
import tech.mogami.facilitator.dto.payment.PaymentDto;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;
import tech.mogami.facilitator.repository.PaymentRepository;
import tech.mogami.facilitator.util.base.BaseService;

import java.util.Objects;
import java.util.Optional;

/**
 * {@link PaymentLogService} implementation.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class PaymentLogServiceImplementation extends BaseService implements PaymentLogService {

    /** Payment repository. */
    private final PaymentRepository paymentRepository;

    @Override
    public void logPaymentStep(final PaymentStepDto paymentStep) {
        log.info("Logging payment step: {}", paymentStep);
        // TODO Use a validator here.
        Objects.requireNonNull(paymentStep, "Payment step must not be null");
        Objects.requireNonNull(paymentStep.nonce(), "Payment nonce must not be null");

        // We get or create the payment, and we add the step.
        final Payment payment = getOrCreatePayment(paymentStep.nonce());
    }

    @Override
    public Optional<PaymentDto> getPaymentById(final String paymentId) {
        log.info("Retrieving payment by ID: {}", paymentId);
        return Optional.empty();
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
