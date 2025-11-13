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
import tech.mogami.facilitator.domain.util.BaseTenantEntity;

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

    /** Payment step type. */
    @Enumerated(STRING)
    @Column(name = "PAYMENT_STEP_TYPE", nullable = false, updatable = false)
    private PaymentStepType paymentStepType;

    /** Request payload sent to the facilitator. */
    @Column(name = "REQUEST_PAYLOAD", nullable = false, updatable = false)
    private String requestPayload;

    /** Response payload sent by the facilitator. */
    @Column(name = "RESPONSE_PAYLOAD", nullable = false, updatable = false)
    private String responsePayload;

    /** Optional error code returned by the facilitator. */
    @Column(name = "ERROR_CODE")
    private String errorCode;

    /** Optional error message returned by the facilitator. */
    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

}
