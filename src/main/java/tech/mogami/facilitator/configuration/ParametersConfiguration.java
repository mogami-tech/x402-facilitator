package tech.mogami.facilitator.configuration;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@SuppressWarnings("checkstyle:DesignForExtension")
public class ParametersConfiguration {
}
