package tech.mogami.facilitator.provider.outbox.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import tech.mogami.facilitator.provider.outbox.domain.OutboxEvent;
import tech.mogami.facilitator.provider.outbox.domain.OutboxEventStatus;
import tech.mogami.facilitator.provider.outbox.domain.OutboxEventType;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for {@link OutboxEvent} entities.
 */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Finds outbox events that are candidates for locking based on their status, lock state, and event types.
     *
     * @param types         List of event types to filter by.
     * @param expiredBefore Instant indicating the cutoff time for expired locks.
     * @param pageable      Pageable object for pagination.
     * @return List of outbox events that are candidates for locking.
     */
    @Query("""
            SELECT e
            FROM OutboxEvent e
            WHERE (
                    e.status = tech.mogami.facilitator.provider.outbox.domain.OutboxEventStatus.PENDING
                    OR (
                        e.status = tech.mogami.facilitator.provider.outbox.domain.OutboxEventStatus.PROCESSING
                        AND (e.lockedBy IS NULL OR e.lockedAt < :expiredBefore)
                    )
                )
            AND e.eventType IN :types
            ORDER BY e.createdAt
            """)
    List<OutboxEvent> findEventsToLock(
            @Param("types") List<OutboxEventType> types,
            @Param("expiredBefore") Instant expiredBefore,
            Pageable pageable
    );

    /**
     * Attempts to lock an outbox event for processing by a specific worker.
     *
     * @param id            The ID of the outbox event to lock.
     * @param workerId      The identifier of the worker attempting to lock the event.
     * @param now           The current timestamp.
     * @param expiredBefore Instant indicating the cutoff time for expired locks.
     * @return The number of rows affected (1 if the lock was successful, 0 otherwise).
     */
    @Transactional
    @Modifying
    @Query("""
            UPDATE OutboxEvent e
            SET     e.lockedBy = :workerId,
                    e.lockedAt = :now,
                    e.status = tech.mogami.facilitator.provider.outbox.domain.OutboxEventStatus.PROCESSING
            WHERE   e.id = :id
                AND (
                        e.status = tech.mogami.facilitator.provider.outbox.domain.OutboxEventStatus.PENDING
                        OR (
                            e.status = tech.mogami.facilitator.provider.outbox.domain.OutboxEventStatus.PROCESSING
                            AND (e.lockedBy IS NULL OR e.lockedAt < :expiredBefore)
                        )
                    )
            """)
    int tryToLockEvent(
            @Param("id") Long id,
            @Param("workerId") String workerId,
            @Param("now") Instant now,
            @Param("expiredBefore") Instant expiredBefore
    );

    /**
     * Resolves an outbox event by updating its status, error message, and processed timestamp.
     *
     * @param eventId      The unique identifier of the outbox event.
     * @param workerId     The identifier of the worker that locked the event.
     * @param status       The new status of the outbox event.
     * @param errorMessage The error message if any occurred during processing.
     * @param processedAt  The timestamp when the event was processed.
     * @return The number of rows affected (1 if the update was successful, 0 otherwise).
     */
    @Transactional
    @Modifying
    @Query("""
            UPDATE  OutboxEvent e
            SET     e.status = :status,
                    e.errorMessage = :errorMessage,
                    e.processedAt = :processedAt,
                    e.lockedBy = NULL,
                    e.lockedAt = NULL
            WHERE   e.eventId = :eventId
              AND   e.lockedBy = :workerId
            """)
    int resolveEvent(
            @Param("eventId") String eventId,
            @Param("workerId") String workerId,
            @Param("status") OutboxEventStatus status,
            @Param("errorMessage") String errorMessage,
            @Param("processedAt") Instant processedAt
    );

}
