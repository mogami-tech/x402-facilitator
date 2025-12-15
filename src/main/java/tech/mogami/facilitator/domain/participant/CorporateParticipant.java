package tech.mogami.facilitator.domain.participant;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static tech.mogami.facilitator.domain.participant.Participant.TYPE_CORPORATION;

/**
 * Represents a corporate client.
 */
@Entity
@DiscriminatorValue(TYPE_CORPORATION)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CorporateParticipant extends Participant {

    /** Name of the corporation. */
    @Column(name = "COMPANY_NAME")
    private String companyName;

}
