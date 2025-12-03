package tech.mogami.facilitator.provider.outbox.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import tech.mogami.facilitator.domain.payment.PaymentStepType;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEventType;
import tech.mogami.facilitator.provider.outbox.handler.OutboxEventMessage;

import static tech.mogami.facilitator.domain.platform.outbox.OutboxEventType.NEW_PAYMENT_STEP;

/**
 * Message representing a new payment step event.
 *
 * @param paymentId       Unique payment identifier
 * @param paymentStepType Type of the payment step
 * @param requestPayload  Request payload sent to the facilitator
 * @param responsePayload Response payload sent by the facilitator
 * @param errorCode       Optional error code returned by the facilitator
 * @param errorMessage    Optional error message returned by the facilitator
 */
@Builder
public record NewPaymentStepMessage(
        @NotNull String paymentId,
        @NotNull PaymentStepType paymentStepType,
        String requestPayload,
        String responsePayload,
        String errorCode,
        String errorMessage
) implements OutboxEventMessage<NewPaymentStepMessage> {

    @Override
    @JsonIgnore
    public OutboxEventType type() {
        return NEW_PAYMENT_STEP;
    }

}
