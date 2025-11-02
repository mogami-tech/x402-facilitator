package tech.mogami.facilitator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.mogami.commons.api.facilitator.supported.SupportedResponse;

import static tech.mogami.commons.constant.network.Networks.BASE_MAINNET;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;
import static tech.mogami.commons.header.payment.schemes.Schemes.EXACT_SCHEME;

/**
 * {@link SupportedService} implementation.
 */
@Slf4j
@Service
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class SupportedServiceImplementation implements SupportedService {

    /** Cached supported response. */
    private final SupportedResponse cachedSupportedResponse;

    public SupportedServiceImplementation() {
        cachedSupportedResponse = SupportedResponse.builder()
                // Base networks =======================================================================================
                .kind(SupportedResponse.SupportedKind.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .scheme(EXACT_SCHEME.name())
                        .network(BASE_SEPOLIA.name())
                        .build())
                .kind(SupportedResponse.SupportedKind.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .scheme(EXACT_SCHEME.name())
                        .network(BASE_MAINNET.name())
                        .build())
                .build();
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
