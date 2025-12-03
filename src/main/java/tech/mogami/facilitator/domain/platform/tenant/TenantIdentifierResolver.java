package tech.mogami.facilitator.domain.platform.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static org.hibernate.cfg.MultiTenancySettings.MULTI_TENANT_IDENTIFIER_RESOLVER;

/**
 * This class is responsible for resolving the tenant identifier in the application.
 */
@Component
@SuppressWarnings({"checkstyle:DesignForExtension"})
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    /** Public tenant ID. */
    private static final String PUBLIC_TENANT_ID = "00000000-0000-0000-0000-000000000000";

    /** Thread-local context for tenant identifier. */
    private static final InheritableThreadLocal<String> CONTEXT = new InheritableThreadLocal<>();

    /**
     * Gets the current tenant identifier.
     *
     * @param tenantId the tenant identifier
     */
    public static void setTenant(final String tenantId) {
        CONTEXT.set(tenantId);
    }

    /**
     * Clears the current tenant identifier.
     */
    public static void clear() {
        CONTEXT.remove();
    }

    @Override
    public String resolveCurrentTenantIdentifier() {
        return Optional.ofNullable(CONTEXT.get()).orElse(PUBLIC_TENANT_ID);
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(final Map<String, Object> hibernateProperties) {
        hibernateProperties.put(MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }

}
