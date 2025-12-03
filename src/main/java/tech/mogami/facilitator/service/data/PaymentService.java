package tech.mogami.facilitator.service.data;

import tech.mogami.facilitator.dto.payment.PaymentDto;

import java.util.Optional;

/**
 * Service interface for managing payment.
 */
public interface PaymentService {

    /**
     * Search a payment by its unique identifier, its nonce.
     *
     * @param paymentId the unique identifier of the payment, its nonce.
     * @return the payment data transfer object
     */
    Optional<PaymentDto> searchPaymentById(String paymentId);

}
