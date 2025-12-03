package tech.mogami.facilitator.provider.outbox.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEvent;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEventType;
import tech.mogami.facilitator.provider.outbox.handler.OutboxEventMessage;
import tech.mogami.facilitator.repository.OutboxEventRepository;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static tech.mogami.facilitator.domain.platform.outbox.OutboxEventStatus.DONE;
import static tech.mogami.facilitator.domain.platform.outbox.OutboxEventStatus.ERROR;

/**
 * {@link OutboxService} implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class OutboxServiceImplementation implements OutboxService {

    /** Datasource. */
    private final DataSource dataSource;

    /** Repository for outbox events. */
    private final OutboxEventRepository outboxEventRepository;

    /** Indicates that the database supports skip locked. */
    private boolean skipLockedEnabled = false;

    @PostConstruct
    public void init() {
        try {
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            skipLockedEnabled = "PostgreSQL".equalsIgnoreCase(databaseProductName);
        } catch (SQLException e) {
            log.error("Impossible to retrieve database metadata {}", e.getMessage());
            skipLockedEnabled = false;
        }
    }

    @Override
    @Transactional
    public void publish(final OutboxEventMessage<?> message) {
        final String eventId = UUID.randomUUID().toString();
        log.debug("Publishing outbox event: eventId={}, eventType={}", eventId, message.type());
        outboxEventRepository.save(OutboxEvent.builder()
                .eventId(eventId)
                .eventType(message.type())
                .payload(JsonUtil.toJson(message))
                .build());
    }

    @Override
    public List<OutboxEvent> fetchPendingEvents(final List<OutboxEventType> eventTypes, final int batchSize) {
        if (skipLockedEnabled) {
            List<String> typeNames = eventTypes.stream()
                    .map(Enum::name)
                    .toList();
            return outboxEventRepository.fetchBatchSkipLocked(typeNames, batchSize);
        } else {
            return outboxEventRepository.fetchBatchPessimistic(
                    eventTypes,
                    PageRequest.of(0, batchSize)
            );
        }
    }

    @Override
    @Transactional
    public void markDone(final OutboxEvent event) {
        event.setStatus(DONE);
        event.setErrorMessage(null);
        event.setProcessedAt(Instant.now());
    }

    @Override
    @Transactional
    public void markError(final OutboxEvent event, final String errorMessage) {
        event.setStatus(ERROR);
        event.setErrorMessage(errorMessage);
        event.setProcessedAt(Instant.now());
    }

}
