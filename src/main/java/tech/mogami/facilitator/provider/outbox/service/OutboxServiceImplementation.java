package tech.mogami.facilitator.provider.outbox.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.provider.outbox.domain.OutboxEvent;
import tech.mogami.facilitator.provider.outbox.domain.OutboxEventStatus;
import tech.mogami.facilitator.provider.outbox.domain.OutboxEventType;
import tech.mogami.facilitator.provider.outbox.handler.OutboxEventMessage;
import tech.mogami.facilitator.provider.outbox.repository.OutboxEventRepository;
import tech.mogami.facilitator.provider.outbox.util.Worker;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;
import static tech.mogami.facilitator.configuration.OutboxConfiguration.DEFAULT_EVENT_LOCK_DURATION;

/**
 * {@link OutboxService} implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class OutboxServiceImplementation implements OutboxService {

    /** Worker id. */
    private final Worker worker;

    /** Repository for outbox events. */
    private final OutboxEventRepository outboxEventRepository;

    @Override
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
    public List<OutboxEvent> lockAndFetchPendingEvents(final List<OutboxEventType> eventTypes, final int batchSize) {
        Instant now = Instant.now();
        Instant expiredBefore = now.minus(DEFAULT_EVENT_LOCK_DURATION);
        return outboxEventRepository.findEventsToLock(
                        eventTypes,
                        expiredBefore,
                        PageRequest.of(0, batchSize * 2))
                .stream()
                .filter(event -> {
                    if (outboxEventRepository.tryToLockEvent(event.getId(), worker.id(), now, expiredBefore) == 1) {
                        log.debug("Locked outbox event with eventId={}", event.getEventId());
                        return true;
                    } else {
                        log.debug("Failed to lock outbox event with eventId={}", event.getEventId());
                        return false;
                    }
                })
                .limit(batchSize)
                .toList();
    }

    @Override
    @Transactional(propagation = REQUIRES_NEW)
    public void markDone(final String eventId) {
        int numbersOfLinesUpdated = outboxEventRepository.resolveEvent(
                eventId,
                worker.id(),
                OutboxEventStatus.DONE,
                null,
                Instant.now()
        );
        if (numbersOfLinesUpdated == 0) {
            log.warn("No outbox event found to mark as DONE for eventId={}", eventId);
        }
    }

    @Override
    @Transactional(propagation = REQUIRES_NEW)
    public void markError(final String eventId, final String errorMessage) {
        int numbersOfLinesUpdated = outboxEventRepository.resolveEvent(
                eventId,
                worker.id(),
                OutboxEventStatus.ERROR,
                errorMessage,
                Instant.now()
        );
        if (numbersOfLinesUpdated == 0) {
            log.warn("No outbox event found to mark as ERROR for eventId={}", eventId);
        }
    }

}
