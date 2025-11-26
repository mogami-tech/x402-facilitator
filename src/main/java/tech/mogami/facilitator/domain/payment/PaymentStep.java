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

    /**
     * Calculates the information score of the payment step.
     *
     * @return the score of the payment step
     */
    @SuppressWarnings("checkstyle:MagicNumber")
    public final int informationScore() {
        return switch (this.paymentStepType) {
            case SETTLE -> {
                if (this.hasNoError()) {
                    yield 7;
                } else {
                    yield 5;
                }
            }
            case VERIFY -> {
                if (this.hasNoError()) {
                    yield 3;
                } else {
                    yield 1;
                }
            }
        };
    }

    /**
     * Checks if the payment step has an error.
     *
     * @return true if there is an error, false otherwise.
     */
    public final boolean hasError() {
        return this.errorCode != null || this.errorMessage != null;
    }

    /**
     * Checks if the payment step has no error.
     *
     * @return true if there is no error, false otherwise.
     */
    public final boolean hasNoError() {
        return !hasError();
    }

}
