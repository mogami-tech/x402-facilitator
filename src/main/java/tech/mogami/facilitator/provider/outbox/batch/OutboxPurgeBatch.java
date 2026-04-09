package tech.mogami.facilitator.provider.outbox.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.mogami.facilitator.provider.outbox.domain.OutboxEvent;
import tech.mogami.facilitator.provider.outbox.domain.OutboxEventStatus;
import tech.mogami.facilitator.provider.outbox.repository.OutboxEventRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static tech.mogami.facilitator.provider.outbox.domain.OutboxEventStatus.DONE;
import static tech.mogami.facilitator.provider.outbox.domain.OutboxEventStatus.ERROR;

/**
 * Outbox purge batch.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class OutboxPurgeBatch {

    /** Retention period in days for DONE status. */
    private static final Duration RETENTION_FOR_DONE_STATUS = Duration.ofDays(10);

    /** Retention period in days for ERROR status. */
    private static final Duration RETENTION_FOR_ERROR_STATUS = Duration.ofDays(100);

    /** Batch size. */
    private static final int BATCH_SIZE = 100;

    /** Outbox event repository. */
    private final OutboxEventRepository outboxEventRepository;

    /**
     * Purges old outbox events based on their status and retention periods.
     */
    @Transactional
    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")
    @SchedulerLock(
            name = "outbox-batch-purge",
            lockAtMostFor = "PT30M",
            lockAtLeastFor = "PT30S"
    )
    public void purge() {
        // Purge DONE events older than retention period ===============================================================
        log.info("Purged {} DONE outbox events older than {} days",
                purgeEvents(DONE, RETENTION_FOR_DONE_STATUS),
                RETENTION_FOR_DONE_STATUS.toDays());

        // Purge ERROR events older than retention period ==============================================================
        log.info("Purged {} ERROR outbox events older than {} days",
                purgeEvents(ERROR, RETENTION_FOR_ERROR_STATUS),
                RETENTION_FOR_ERROR_STATUS.toDays());
    }

    /**
     * Purges outbox events with the specified status that are older than the given retention duration.
     *
     * @param outboxEventStatus the status of the outbox events to purge
     * @param retentionDuration the retention duration
     * @return the total number of deleted events
     */
    private int purgeEvents(final OutboxEventStatus outboxEventStatus,
                            final Duration retentionDuration) {
        int totalDeleted = 0;
        while (true) {
            Page<OutboxEvent> page = outboxEventRepository.findByStatusInAndProcessedAtBefore(
                    Set.of(outboxEventStatus),
                    Instant.now().minus(retentionDuration),
                    PageRequest.of(0, BATCH_SIZE));
            if (page.isEmpty()) {
                // We are done.
                break;
            } else {
                // There are more events to delete.
                outboxEventRepository.deleteAllInBatch(page.getContent());
                totalDeleted += page.getNumberOfElements();
            }
        }
        return totalDeleted;
    }

}
