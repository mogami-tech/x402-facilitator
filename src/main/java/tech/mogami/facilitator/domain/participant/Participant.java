package tech.mogami.facilitator.domain.participant;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.mogami.facilitator.domain.util.BaseEntity;

import static jakarta.persistence.DiscriminatorType.STRING;
import static jakarta.persistence.InheritanceType.SINGLE_TABLE;

/**
 * Represents a participant.
 */
@Entity
@Inheritance(strategy = SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = STRING)
@Table(name = "CLIENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Participant extends BaseEntity {

    /** Participant type: AGENT. */
    public static final String TYPE_AGENT = "AGENT";

    /** Participant type: CORPORATION. */
    public static final String TYPE_CORPORATION = "CORPORATION";

    /** Participant type: INDIVIDUAL. */
    public static final String TYPE_INDIVIDUAL = "INDIVIDUAL";

    /** Participant identifier. */
    @Column(name = "PARTICIPANT_ID", nullable = false, unique = true)
    private String participantId;

    /** The email of the participant. */
    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

}
