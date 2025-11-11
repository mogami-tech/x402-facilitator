package tech.mogami.facilitator.domain.util;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TenantId;

/**
 * Base tenant entity class to be extended by other tenant-specific entities.
 */
@MappedSuperclass
@Getter
@Setter
public class BaseTenantEntity extends BaseEntity {

    /** Tenant. */
    @TenantId
    @Column(name = "FK_TENANT_TENANT_ID", nullable = false, updatable = false)
    private String tenantId;

}
