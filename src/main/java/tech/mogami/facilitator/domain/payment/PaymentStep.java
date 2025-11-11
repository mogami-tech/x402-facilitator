package tech.mogami.facilitator.domain.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.mogami.commons.constant.EventType;
import tech.mogami.facilitator.domain.util.BaseTenantEntity;

import java.time.Instant;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.EAGER;

/**
 * Represents a payment step.
 */
@Entity
@Table(name = "PAYMENT_STEP")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStep extends BaseTenantEntity {

    /** Unique payment step id. */
    @Column(name = "PAYMENT_STEP_ID", nullable = false, unique = true, updatable = false)
    private String paymentStepId;

    /** Corresponding payment. */
    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "FK_PAYMENT_ID", nullable = false)
    private Payment payment;

    /** Payment step event type. */
    @Enumerated(STRING)
    @Column(name = "EVENT_TYPE", nullable = false, updatable = false)
    private EventType eventType;

    /** Payment step payload. */
    @Column(name = "PAYLOAD", nullable = false, updatable = false)
    private String payload;

    /** Payment step error message. */
    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    /** Payment step timestamp. */
    @Column(name = "OCCURRED_AT", nullable = false, updatable = false)
    private Instant timestamp;

}
