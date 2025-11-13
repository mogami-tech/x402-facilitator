package tech.mogami.facilitator.domain.util;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.TenantId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base tenant entity class to be extended by other tenant-specific entities.
 */
@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class BaseTenantEntity extends BaseEntity {

    /** Tenant. */
    @TenantId
    @Column(name = "FK_TENANT_TENANT_ID", nullable = false, updatable = false)
    private String tenantId;

    /** Entity creation date. */
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Instant createdAt;

    /** Entity last modification date. */
    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private Instant updatedAt;

}
