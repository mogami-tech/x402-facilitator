package tech.mogami.facilitator.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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

    /**
     * Facilitator address.
     *
     * @return Credentials
     */
    @Bean
    public String facilitatorAddress() {
        final String facilitatorAddress = Credentials.create(x402Parameters.facilitator().privateKey()).getAddress();
        log.info("[Configuration] Facilitator address is {}", facilitatorAddress);
        return facilitatorAddress;
    }

}
