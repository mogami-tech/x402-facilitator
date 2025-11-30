package tech.mogami.facilitator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import tech.mogami.facilitator.domain.queue.PaymentQueueElement;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for managing payment queue element.
 */
public interface PaymentQueueElementRepository extends JpaRepository<PaymentQueueElement, Long> {

    /**
     * Lock and fetch the oldest queue element for processing (multi-thread safe).
     *
     * @return an Optional containing the locked PaymentQueueElement, or empty if none are available
     */
    @Query(value = """
            SELECT      *
            FROM        QUEUE_PAYMENT_TO_UPDATE
            ORDER BY    CREATED_AT
            LIMIT 1
            FOR UPDATE SKIP LOCKED
            """,
            nativeQuery = true
    )
    @Transactional
    Optional<PaymentQueueElement> lockOneElement();

    /**
     * Enqueue a payment for processing. If the payment is already in the queue, update its createdAt timestamp.
     *
     * @param paymentId the unique payment ID
     * @param createdAt the timestamp when the payment was enqueued
     */
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO QUEUE_PAYMENT_TO_UPDATE (payment_id, created_at)
            VALUES (:paymentId, :createdAt)
            ON CONFLICT (payment_id)
            DO UPDATE SET created_at = :createdAt
            """, nativeQuery = true)
    void enqueuePayment(@Param("paymentId") String paymentId, @Param("createdAt") Instant createdAt);

}
