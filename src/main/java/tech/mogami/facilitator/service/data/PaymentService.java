package tech.mogami.facilitator.service.data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import tech.mogami.facilitator.dto.payment.PaymentDto;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;

import java.util.Optional;

/**
 * Service interface for managing payment.
 */
public interface PaymentService {

    /**
     * Logs a payment step.
     *
     * @param paymentStep the payment step to log
     */
    void logPaymentStep(@NotNull @Valid PaymentStepDto paymentStep);

    /**
     * Search a payment by its unique identifier, its nonce.
     *
     * @param paymentId the unique identifier of the payment, its nonce.
     * @return the payment data transfer object
     */
    Optional<PaymentDto> searchPaymentById(String paymentId);

}
