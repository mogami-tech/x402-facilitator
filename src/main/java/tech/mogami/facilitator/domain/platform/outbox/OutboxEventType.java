package tech.mogami.facilitator.domain.platform.outbox;

import tech.mogami.facilitator.provider.outbox.event.NewPaymentStepMessage;
import tech.mogami.facilitator.provider.outbox.handler.OutboxEventMessage;

/**
 * Enumeration representing the type of outbox event.
 */
public enum OutboxEventType {

    /** A new payment step event is here. */
    NEW_PAYMENT_STEP(NewPaymentStepMessage.class);

    /** Placeholder for other event types. */
    private final Class<? extends OutboxEventMessage<?>> payloadType;

    /**
     * Constructs an OutboxEventType with the specified payload type.
     * *
     *
     * @param newPayloadType the class type of the event payload
     */
    OutboxEventType(final Class<? extends OutboxEventMessage<?>> newPayloadType) {
        this.payloadType = newPayloadType;
    }

    /**
     * Returns the class type of the event payload associated with this event type.
     *
     * @return the Class of the payload type
     */
    public Class<? extends OutboxEventMessage<?>> payloadType() {
        return payloadType;
    }

}
