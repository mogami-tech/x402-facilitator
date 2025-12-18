package tech.mogami.facilitator.provider.outbox.service;

import jakarta.validation.constraints.NotNull;
import tech.mogami.facilitator.provider.outbox.domain.OutboxEvent;
import tech.mogami.facilitator.provider.outbox.domain.OutboxEventType;
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
     * Locks and fetches pending outbox events of specified types.
     *
     * @param eventTypes the list of outbox event types to fetch
     * @param batchSize  the maximum number of events to fetch
     * @return the list of locked outbox events
     */
    List<OutboxEvent> lockAndFetchPendingEvents(@NotNull List<OutboxEventType> eventTypes, int batchSize);

    /**
     * Marks the outbox event with the specified ID as done.
     *
     * @param eventId the ID of the outbox event to mark as done
     */
    void markDone(String eventId);

    /**
     * Marks the outbox event with the specified ID as error with the given error message.
     *
     * @param eventId      the ID of the outbox event to mark as error
     * @param errorMessage the error message to associate with the event
     */
    void markError(String eventId, String errorMessage);

}
