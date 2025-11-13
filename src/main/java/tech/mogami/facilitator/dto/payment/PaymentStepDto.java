package tech.mogami.facilitator.dto.payment;

import tech.mogami.facilitator.domain.payment.PaymentStepType;

/**
 * Payment step data transfer object.
 *
 * @param paymentStepId   Unique payment step identifier
 * @param paymentStepType Type of the payment step
 * @param requestPayload  Request payload sent to the facilitator
 * @param responsePayload Response payload sent by the facilitator
 * @param errorCode       Optional error code returned by the facilitator
 * @param errorMessage    Optional error message returned by the facilitator
 */
public record PaymentStepDto(
        String paymentStepId,
        PaymentStepType paymentStepType,
        String requestPayload,
        String responsePayload,
        String errorCode,
        String errorMessage
) {
}
