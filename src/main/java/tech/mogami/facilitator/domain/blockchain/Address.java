package tech.mogami.facilitator.domain.blockchain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import tech.mogami.facilitator.domain.participant.Participant;
import tech.mogami.facilitator.domain.util.base.BaseEntity;

import static jakarta.persistence.FetchType.EAGER;
import static lombok.AccessLevel.PACKAGE;

/**
 * Address on blockchain.
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@AllArgsConstructor(access = PACKAGE)
@Builder
@Table(name = "ADDRESS")
public class Address extends BaseEntity {

    /** Blockchain address. */
    @Column(name = "ADDRESS")
    private String address;

    /** Participant owning that address. */
    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "FK_PARTICIPANT_ID")
    private Participant participant;

}
