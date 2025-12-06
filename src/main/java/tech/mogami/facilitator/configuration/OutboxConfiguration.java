package tech.mogami.facilitator.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEventType;
import tech.mogami.facilitator.provider.outbox.handler.OutboxEventHandler;
import tech.mogami.facilitator.provider.outbox.util.WorkerId;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Outbox configuration.
 */
@Slf4j
@Configuration
public class OutboxConfiguration {

    /** Default event lock duration. */
    public static final Duration DEFAULT_EVENT_LOCK_DURATION = Duration.ofMinutes(1);

    /**
     * Provides a worker ID.
     *
     * @return the worker ID
     */
    @Bean
    public WorkerId workerId() {
        return new WorkerId(UUID.randomUUID().toString());
    }

    /**
     * Registers outbox event handlers.
     *
     * @param newDiscoveredHandlers the list of discovered outbox event handlers
     * @return a map of outbox event handlers
     */
    @Bean
    public Map<OutboxEventType, OutboxEventHandler<?>> outboxEventHandlers(final List<OutboxEventHandler<?>> newDiscoveredHandlers) {
        return newDiscoveredHandlers.stream()
                .peek(outboxEventHandler -> log.info("registering handler for {}", outboxEventHandler.getClass()))
                .collect(Collectors.toMap(
                        OutboxEventHandler::supports,
                        Function.identity()
                ));
    }

}
