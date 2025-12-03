package tech.mogami.facilitator.provider.outbox.service;

import jakarta.validation.constraints.NotNull;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEvent;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEventType;
import tech.mogami.facilitator.provider.outbox.handler.OutboxEventMessage;

import java.util.List;

/**
 * Outbox service interface.
 */
public interface OutboxService {

    /**
     * Publishes an outbox event message.
     *
     * @param message the outbox event message to publish
     */
    void publish(@NotNull OutboxEventMessage<?> message);

    /**
     * Fetches pending outbox events of the specified types up to the given batch size (using lock).
     *
     * @param eventTypes the list of outbox event types to fetch
     * @param batchSize  the maximum number of events to fetch
     * @return the list of pending outbox events
     */
    List<OutboxEvent> fetchPendingEvents(@NotNull List<OutboxEventType> eventTypes, int batchSize);

    /**
     * Marks the given outbox event as done.
     *
     * @param event the outbox event to mark as done
     */
    void markDone(@NotNull OutboxEvent event);

    /**
     * Marks the given outbox event as errored with the provided error message.
     *
     * @param event        the outbox event to mark as errored
     * @param errorMessage the error message associated with the event
     */
    void markError(@NotNull OutboxEvent event, String errorMessage);

}
