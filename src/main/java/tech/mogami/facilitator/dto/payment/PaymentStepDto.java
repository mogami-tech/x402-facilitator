package tech.mogami.facilitator.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import tech.mogami.facilitator.domain.payment.PaymentStepType;

import java.time.Instant;

/**
 * Payment step data transfer object.
 *
 * @param paymentStepId   Unique payment step identifier
 * @param paymentStepType Type of the payment step
 * @param requestPayload  Request payload sent to the facilitator
 * @param responsePayload Response payload sent by the facilitator
 * @param errorCode       Optional error code returned by the facilitator
 * @param errorMessage    Optional error message returned by the facilitator
 * @param createdAt       Timestamp when the payment step was created
 */
@Builder
public record PaymentStepDto(
        String paymentStepId,
        @NotNull PaymentStepType paymentStepType,
        String requestPayload,
        String responsePayload,
        String errorCode,
        String errorMessage,
        Instant createdAt
) {

    /**
     * Indicates if the payment step has an error.
     *
     * @return true if there is an error, false otherwise
     */
    public boolean hasError() {
        return errorCode != null || errorMessage != null;
    }

}

