package tech.mogami.facilitator.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.facilitator.service.facilitator.SupportedService;

import java.util.Map;
import java.util.stream.Collectors;

import static tech.mogami.commons.constant.network.Networks.ALL_NETWORKS;

/**
 * Configuration class for Web3j integration.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class Web3jConfiguration {

    /** Supported service. */
    private final SupportedService supportedService;

    /**
     * Creates a map of Web3j clients for all supported networks.
     *
     * @return Map of Network to Web3j client
     */
    @Bean
    public Map<Network, Web3j> web3jClients() {
        // TODO Don't use ALL_NETWORKS_BY_NAME but use the supported service.
        return ALL_NETWORKS.stream()
                .filter(Network::isEvm)
                .peek(network -> log.info("[Configuration] Network {} uses {} as rpc server", network.name(), network.rpcUrl()))
                .collect(Collectors.toMap(
                        network -> network,
                        network -> Web3j.build(new HttpService(network.rpcUrl()))
                ));
    }

}
