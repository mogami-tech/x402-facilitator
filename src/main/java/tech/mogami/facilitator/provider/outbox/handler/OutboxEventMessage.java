package tech.mogami.facilitator.provider.outbox.handler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEventType;

/**
 * Marker interface for outbox event messages.
 *
 * @param <T> the type of the message
 */
public interface OutboxEventMessage<T> {

    /**
     * Returns the type of the outbox event.
     *
     * @return the OutboxEventType
     */
    @JsonIgnore
    OutboxEventType type();

    /**
     * Returns the class type of the event payload.
     *
     * @return the Class of the payload type
     */
    @JsonIgnore
    @SuppressWarnings("unchecked")
    default Class<T> payloadType() {
        return (Class<T>) this.getClass();
    }

}
