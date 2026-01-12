package tech.mogami.facilitator.service.facilitator;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.mogami.commons.api.facilitator.supported.SupportedResponse;

import java.util.List;

import static tech.mogami.commons.constant.network.Networks.BASE_MAINNET;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;
import static tech.mogami.commons.payment.schemes.Schemes.EXACT_SCHEME;

/**
 * {@link SupportedService} implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class SupportedServiceImplementation implements SupportedService {

    /** Facilitator address. */
    private final String facilitatorAddress;

    /** Cached supported response. */
    private SupportedResponse cachedSupportedResponse;

    @PostConstruct
    private void postConstruct() {
        cachedSupportedResponse = SupportedResponse.builder()
                .kind(SupportedResponse.SupportedKind.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .scheme(EXACT_SCHEME.name())
                        .network(BASE_SEPOLIA.networkId())
                        .build())
                .kind(SupportedResponse.SupportedKind.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .scheme(EXACT_SCHEME.name())
                        .network(BASE_MAINNET.networkId())
                        .build())
                .signer("eip155:*", List.of(facilitatorAddress))
                .build();
        log.info("SupportedService initialized with supported response: {}", cachedSupportedResponse);
    }

    /**
     * Returns supported payment schemes and networks.
     *
     * @return SupportedResponse
     */
    @Override
    public SupportedResponse supported() {
        return cachedSupportedResponse;
    }

}
