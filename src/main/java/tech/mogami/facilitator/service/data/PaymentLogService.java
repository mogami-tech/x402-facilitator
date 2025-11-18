package tech.mogami.facilitator.service.data;

import tech.mogami.facilitator.dto.payment.PaymentDto;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;

import java.util.Optional;

/**
 * Service interface for managing payment logs.
 */
public interface PaymentLogService {

    /**
     * Logs a payment step.
     *
     * @param paymentStep the payment step to log
     */
    void logPaymentStep(PaymentStepDto paymentStep);

    /**
     * Retrieves a payment by its unique identifier, its nonce.
     *
     * @param paymentId the unique identifier of the payment, its nonce.
     * @return the payment data transfer object
     */
    Optional<PaymentDto> getPaymentById(String paymentId);

}
