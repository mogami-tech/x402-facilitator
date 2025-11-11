package tech.mogami.facilitator.domain.participant;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static tech.mogami.facilitator.domain.participant.Participant.TYPE_INDIVIDUAL;

/**
 * Represents an individual client.
 */
@Entity
@DiscriminatorValue(TYPE_INDIVIDUAL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndividualParticipant extends Participant {

    /** First name of the individual client. */
    @Column(name = "FIRST_NAME")
    private String firstName;

    /** Last name of the individual client. */
    @Column(name = "LAST_NAME")
    private String lastName;

}
