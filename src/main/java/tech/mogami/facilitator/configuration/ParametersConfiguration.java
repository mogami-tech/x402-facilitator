package tech.mogami.facilitator.configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import tech.mogami.facilitator.parameter.X402Parameters;

import static tech.mogami.commons.constant.network.Networks.ALL_NETWORKS;

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

    @PostConstruct
    public final void init() {
        ALL_NETWORKS.forEach(network -> log.info("[Configuration] Network {} uses {} as rpc server", network.name(), network.rpcUrl()));
    }

}
