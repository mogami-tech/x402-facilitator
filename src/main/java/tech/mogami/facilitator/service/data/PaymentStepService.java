package tech.mogami.facilitator.service.data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;

/**
 * Service interface for managing payment steps.
 */
public interface PaymentStepService {

    /**
     * Add a payment step asynchronously.
     *
     * @param paymentId   the ID of the payment
     * @param paymentStep the payment step data transfer object
     */
    void addPaymentStep(@NotNull String paymentId,
                        @NotNull @Valid PaymentStepDto paymentStep);

}
