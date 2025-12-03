package tech.mogami.facilitator.domain.platform.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
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

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static tech.mogami.facilitator.domain.platform.outbox.OutboxEventStatus.PENDING;

/**
 * Represents an outbox event.
 */
@Entity
@Table(name = "OUTBOX_EVENT")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    /** Unique identifier. */
    @Id
    @Column(name = "ID", updatable = false)
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    /** Type of the outbox event. */
    @Enumerated(STRING)
    @Column(name = "EVENT_TYPE", nullable = false, updatable = false)
    private OutboxEventType eventType;

    /** Payload of the outbox event. */
    @Column(name = "PAYLOAD", nullable = false, updatable = false)
    private String payload;

    /** Status of the outbox event. */
    @Builder.Default
    @Enumerated(STRING)
    @Column(name = "EVENT_STATUS", nullable = false, updatable = false)
    private OutboxEventStatus status = PENDING;

    /** Error message if any occurred during processing. */
    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    /** Created at timestamp. */
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Instant createdAt;

    /** Processed at timestamp. */
    @Column(name = "PROCESSED_AT")
    private Instant processedAt;

    /**
     * Sets the createdAt timestamp before persisting.
     */
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

}
