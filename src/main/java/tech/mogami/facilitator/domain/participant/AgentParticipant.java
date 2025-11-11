package tech.mogami.facilitator.domain.participant;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static tech.mogami.facilitator.domain.participant.Participant.TYPE_AGENT;

/**
 * Represents an autonomous AI agent participant.
 */
@Entity
@DiscriminatorValue(TYPE_AGENT)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentParticipant extends Participant {

    /** Name of the AI agent. */
    @Column(name = "AGENT_NAME")
    private String agentName;

}
