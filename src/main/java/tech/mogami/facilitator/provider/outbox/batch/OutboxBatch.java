package tech.mogami.facilitator.provider.outbox.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEvent;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEventType;
import tech.mogami.facilitator.provider.outbox.handler.OutboxEventHandler;
import tech.mogami.facilitator.provider.outbox.handler.OutboxEventHandlerResult;
import tech.mogami.facilitator.provider.outbox.service.OutboxService;

import java.util.List;
import java.util.Map;

/**
 * Abstract class for outbox batches.
 */
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public abstract class OutboxBatch {

    /** Default fixed delay for scheduling batches. */
    protected static final long DEFAULT_FIXED_DELAY_MS = 200L;

    /** Map of outbox event handlers. */
    private final Map<OutboxEventType, OutboxEventHandler<?>> handlers;

    /** Outbox service. */
    private final OutboxService outboxService;

    /**
     * Returns the list of supported outbox event types.
     *
     * @return the list of supported OutboxEventType
     */
    protected abstract List<OutboxEventType> supportedTypes();

    /**
     * Returns the batch size for processing outbox events.
     * 1 by default.
     *
     * @return the batch size
     */
    @SuppressWarnings("SameReturnValue")
    protected int batchSize() {
        return 1;
    }

    /**
     * Scheduled method to run the batch processing.
     */
    @Scheduled(fixedDelay = DEFAULT_FIXED_DELAY_MS)
    public void run() {
        final List<OutboxEvent> outboxEvents = outboxService.lockAndFetchPendingEvents(supportedTypes(), batchSize());
        outboxEvents.stream()
                .peek(event -> log.debug("Processing outbox event: eventId: {}", event.getEventId()))
                .forEach(event -> {

                    // Process the event ===============================================================================
                    final OutboxEventHandler<?> handler = handlers.get(event.getEventType());
                    final Object payload = JsonUtil.fromJson(event.getPayload(), event.getEventType().payloadType());
                    OutboxEventHandlerResult callResult = callHandler(handler, payload);

                    if (callResult.success()) {
                        log.info("Outbox event {} processed successfully", event.getEventId());
                        outboxService.markDone(event.getEventId());
                    } else {
                        log.info("Outbox event {} processing failed", event.getEventId());
                        outboxService.markError(event.getEventId(), callResult.errorMessage());
                    }

                });
    }

    /**
     * Calls the handler with the given payload.
     *
     * @param handler the outbox event handler
     * @param payload the event payload
     * @param <T>     the type of the payload
     * @return the result of the handling
     */
    @SuppressWarnings("unchecked")
    private <T> OutboxEventHandlerResult callHandler(final OutboxEventHandler<T> handler, final Object payload) {
        return handler.handle((T) payload);
    }

}
