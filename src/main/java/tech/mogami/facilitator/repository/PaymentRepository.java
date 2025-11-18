package tech.mogami.facilitator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.mogami.facilitator.domain.payment.Payment;

import java.util.Optional;

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
    Optional<Payment> findByPaymentId(String paymentId);

}
