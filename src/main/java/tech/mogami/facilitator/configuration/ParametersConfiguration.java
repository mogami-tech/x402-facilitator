package tech.mogami.facilitator.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import tech.mogami.facilitator.parameter.X402Parameters;

/**
 * Parameter configuration for the X402 facilitator.
 */
@Configuration
@EnableConfigurationProperties({
        X402Parameters.class
})
public class ParametersConfiguration {
}
