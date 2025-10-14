package tech.mogami.facilitator.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import tech.mogami.facilitator.parameter.X402Parameters;

/**
 * Parameter configuration.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({
        X402Parameters.class
})
@RequiredArgsConstructor
public class ParametersConfiguration {

    /** X402 parameters. */
    private final X402Parameters x402Parameters;

    @PostConstruct
    public final void init() {
        final Credentials credentials = Credentials.create(x402Parameters.facilitator().privateKey());
        log.info("[X402Parameters] Facilitator address: {}", credentials.getAddress());
    }

}
