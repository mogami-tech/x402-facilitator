package tech.mogami.facilitator.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tech.mogami.facilitator.domain.payment.Payment;

import java.util.List;
import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

/**
 * Repository interface for managing {@link Payment} entities.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds a payment by its unique payment ID.
     *
     * @param paymentId the unique payment ID
     * @return an Optional containing the found Payment, or empty if not found
     */
    @Lock(PESSIMISTIC_WRITE)
    Optional<Payment> findByPaymentId(String paymentId);

    /**
     * Streams payments that need to be updated.
     * A payment needs processing if it has never been updated or if it has steps created after its last update.
     *
     * @param pageable pagination information
     * @return a Stream of paymentId to be processed
     */
    @Query("""
            SELECT      p.paymentId
            FROM        Payment p
            JOIN        p.steps s
            WHERE       p.updatedAt IS NULL OR s.createdAt > p.updatedAt
            GROUP BY    p.id
            ORDER BY    MAX(s.createdAt) DESC
            """)
    List<String> paymentsToUpdate(Pageable pageable);

}
