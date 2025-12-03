package tech.mogami.facilitator.domain.platform.tenant;

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
import org.hibernate.annotations.CreationTimestamp;
import tech.mogami.facilitator.domain.platform.base.BaseEntity;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.EAGER;

/**
 * Represents a tenant API key.
 */
@Entity
@Table(name = "TENANT_API_KEY")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TenantApiKey extends BaseEntity {

    /** API key hash. */
    @Column(name = "API_KEY_HASH", nullable = false, unique = true)
    private String apiKeyHash;

    /** API key name. */
    @Column(name = "NAME")
    private String name;

    /** Corresponding tenant. */
    @ManyToOne(fetch = EAGER)
    @JoinColumn(name = "FK_TENANT_ID", nullable = false)
    private Tenant tenant;

    /** Revoked. */
    @Builder.Default
    @Column(name = "REVOKED", nullable = false)
    private boolean revoked = false;

    /** Creation timestamp. */
    @CreationTimestamp
    @Column(name = "CREATED_ON", nullable = false, updatable = false)
    private LocalDateTime createdOn;

}
