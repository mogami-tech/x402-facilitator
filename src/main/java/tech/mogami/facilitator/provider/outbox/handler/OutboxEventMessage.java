package tech.mogami.facilitator.provider.outbox.handler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.mogami.facilitator.provider.outbox.domain.OutboxEventType;

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
    @SuppressWarnings("SameReturnValue")
    OutboxEventType type();

}
