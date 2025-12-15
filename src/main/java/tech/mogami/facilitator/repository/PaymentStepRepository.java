package tech.mogami.facilitator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.mogami.facilitator.domain.payment.PaymentStep;

/**
 * Repository interface for managing {@link PaymentStep} entities.
 */
@Repository
public interface PaymentStepRepository extends JpaRepository<PaymentStep, Long> {
}
