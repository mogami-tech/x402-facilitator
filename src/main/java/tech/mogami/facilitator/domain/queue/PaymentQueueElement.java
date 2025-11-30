package tech.mogami.facilitator.domain.queue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * Entity representing an element in the payment update queue.
 */
@Entity
@Table(name = "QUEUE_PAYMENT_TO_UPDATE")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentQueueElement {

    /** Unique identifier for the payment queue entry. */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "ID", nullable = false, updatable = false)
    private Long id;

    /** Unique payment identifier associated with this queue entry. */
    @Column(name = "PAYMENT_ID", nullable = false, updatable = false)
    private String paymentId;

    /** Timestamp indicating when the queue entry was created. */
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Lifecycle callback to set the creation timestamp before persisting.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

}
