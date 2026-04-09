package tech.mogami.facilitator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.mogami.facilitator.domain.payment.PaymentStep;

/**
 * Repository interface for managing {@link PaymentStep} entities.
 */
public interface PaymentStepRepository extends JpaRepository<PaymentStep, Long> {

}
