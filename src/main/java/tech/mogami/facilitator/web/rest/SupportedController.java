package tech.mogami.facilitator.web.rest;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.commons.api.facilitator.supported.SupportedResponse;
import tech.mogami.commons.api.facilitator.supported.SupportedResponse.SupportedKind;

import static tech.mogami.commons.api.facilitator.FacilitatorRoutes.SUPPORTED_URL;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;
import static tech.mogami.commons.header.payment.schemes.Schemes.EXACT_SCHEME;

/**
 * /supported endpoint - Get supported payment schemes and networks.
 */
@RestController
public class SupportedController {

    /**
     * Returns supported payment schemes and networks.
     *
     * @return SupportedResponse
     */
    @GetMapping(SUPPORTED_URL)
    @Cacheable(value = "supportedCache", key = "'supported'", sync = true)
    public SupportedResponse supported() {
        return SupportedResponse.builder()
                // Base networks =======================================================================================
                .kind(SupportedKind.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .scheme(EXACT_SCHEME.name())
                        .network(BASE_SEPOLIA.name())
                        .build())
                .build();
    }

}
