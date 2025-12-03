package tech.mogami.facilitator.provider.outbox.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEventType;
import tech.mogami.facilitator.provider.outbox.handler.OutboxEventHandler;
import tech.mogami.facilitator.provider.outbox.service.OutboxService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract class for outbox batches.
 */
@Slf4j
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public abstract class OutboxBatch {

    /** Default fixed delay for scheduling batches. */
    protected static final long DEFAULT_FIXED_DELAY_MS = 200L;

    /** Outbox service. */
    private final OutboxService outboxService;

    /** Map of outbox event handlers. */
    private final Map<OutboxEventType, OutboxEventHandler<?>> handlers = new HashMap<>();

    /**
     * Constructor.
     *
     * @param newOutboxService      the outbox service
     * @param newDiscoveredHandlers the list of discovered outbox event handlers
     */
    public OutboxBatch(final OutboxService newOutboxService,
                       final List<OutboxEventHandler<?>> newDiscoveredHandlers) {
        this.outboxService = newOutboxService;
        this.handlers.putAll(newDiscoveredHandlers.stream()
                .filter(h -> supportedTypes().contains(h.supports()))
                .peek(outboxEventHandler -> log.info("registering handler for {}", outboxEventHandler.getClass()))
                .collect(Collectors.toMap(
                        OutboxEventHandler::supports,
                        Function.identity()
                )));
    }

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
    protected int batchSize() {
        return 1;
    }

    /**
     * Processes a batch of outbox events.
     */
    protected void processEvents() {
        outboxService.fetchPendingEvents(supportedTypes(), batchSize())
                .stream()
                .peek(event -> log.info("processing event  {}", event.getId()))
                .forEach(event -> {
                    try {
                        final OutboxEventHandler<?> handler = handlers.get(event.getEventType());
                        final Object payload = JsonUtil.fromJson(event.getPayload(), event.getEventType().payloadType());
                        callHandler(handler, payload);
                        outboxService.markDone(event);
                    } catch (Exception e) {
                        outboxService.markError(event, e.getMessage());
                    }
                });
    }

    /**
     * Scheduled method to run the batch processing.
     */
    @Scheduled(fixedDelay = DEFAULT_FIXED_DELAY_MS)
    public void run() {
        processEvents();
    }

    /**
     * Calls the handler with the given payload.
     *
     * @param handler the outbox event handler
     * @param payload the event payload
     * @param <T>     the type of the payload
     */
    @SuppressWarnings("unchecked")
    private <T> void callHandler(final OutboxEventHandler<T> handler, final Object payload) {
        handler.handle((T) payload);
    }

}
