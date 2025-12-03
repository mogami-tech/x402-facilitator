package tech.mogami.facilitator.provider.outbox.handler;


import tech.mogami.facilitator.domain.platform.outbox.OutboxEventType;

/**
 * Generic interface for handling outbox events.
 *
 * @param <T> the type of the message
 */
public interface OutboxEventHandler<T> {

    /**
     * Returns the type of outbox event this handler supports.
     *
     * @return the supported OutboxEventType
     */
    OutboxEventType supports();

    /**
     * Handles the given event payload.
     *
     * @param payload the event payload to handle
     */
    void handle(T payload);

}
