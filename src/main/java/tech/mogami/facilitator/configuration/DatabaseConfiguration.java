package tech.mogami.facilitator.configuration;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Database configuration.
 */
@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class DatabaseConfiguration {

    /** Data source. */
    private final DataSource dataSource;

    /**
     * Lock provider for ShedLock using JDBC template.
     *
     * @return LockProvider
     */
    @Bean
    public LockProvider lockProvider() {
        return new JdbcTemplateLockProvider(
                JdbcTemplateLockProvider.Configuration.builder()
                        .withJdbcTemplate(new JdbcTemplate(dataSource))
                        .usingDbTime() // IMPORTANT
                        .build()
        );
    }

}
