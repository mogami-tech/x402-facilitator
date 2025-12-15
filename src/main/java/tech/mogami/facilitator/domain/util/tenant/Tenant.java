package tech.mogami.facilitator.domain.util.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tech.mogami.facilitator.domain.participant.Participant;
import tech.mogami.facilitator.domain.util.base.BaseEntity;

import static jakarta.persistence.FetchType.EAGER;

/**
 * Represents a tenant.
 */
@Entity
@Table(name = "TENANT")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tenant extends BaseEntity {

    /** Tenant identifier. */
    @Column(name = "TENANT_ID", nullable = false, unique = true)
    private String tenantId;

    /** Tenant owner. */
    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "FK_PARTICIPANT_ID", nullable = false)
    private Participant participant;

    /** Tenant name. */
    @Column(name = "NAME")
    private String name;

}
