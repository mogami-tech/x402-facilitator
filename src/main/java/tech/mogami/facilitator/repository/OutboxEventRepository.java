package tech.mogami.facilitator.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEvent;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEventType;

import java.util.List;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

/**
 * Repository interface for {@link OutboxEvent} entities.
 */
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * Fetches a batch of pending outbox events of specified types, locking the selected rows to prevent concurrent processing.
     * Works with PostgreSQL database.
     *
     * @param types the list of event types to filter by
     * @param limit the maximum number of events to fetch
     * @return a list of pending outbox events
     */
    @Query(value = """
            SELECT      *
            FROM        OUTBOX_EVENT
            WHERE       status = 'PENDING'
              AND       event_type IN (:types)
            ORDER BY    created_at
            FOR UPDATE SKIP LOCKED
            LIMIT :limit
            """,
            nativeQuery = true)
    List<OutboxEvent> fetchBatchSkipLocked(
            @Param("types") List<String> types,
            @Param("limit") int limit);

    /**
     * Fetches a batch of pending outbox events of specified types with pessimistic locking.
     * Works with hsqlDB database.
     *
     * @param types    the list of event types to filter by
     * @param pageable the pagination information
     * @return a list of pending outbox events
     */
    @Lock(PESSIMISTIC_WRITE)
    @Query("""
            SELECT      e
            FROM        OutboxEvent e
            WHERE       e.status = 'PENDING'
              AND       e.eventType IN :types
            ORDER BY    e.createdAt
            """)
    List<OutboxEvent> fetchBatchPessimistic(
            @Param("types") List<OutboxEventType> types,
            Pageable pageable);

}
