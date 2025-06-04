package tech.mogami.facilitator.web.rest;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.commons.api.facilitator.supported.SupportedResponse;
import tech.mogami.commons.api.facilitator.supported.SupportedResponse.SupportedKind;

import static tech.mogami.commons.api.facilitator.FacilitatorRoutes.SUPPORTED_URL;
import static tech.mogami.commons.constant.X402Constants.X402_SUPPORTED_VERSION;
import static tech.mogami.commons.constant.networks.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.header.payment.schemes.ExactSchemeConstants.EXACT_SCHEME_NAME;

/**
 * /supported endpoint.
 */
@RestController
public class SupportedController {

    /**
     * Returns a response indicating what is supported.
     *
     * @return SupportedResponse
     */
    @GetMapping(SUPPORTED_URL)
    @Cacheable(value = "supportedCache", key = "'supported'", sync = true)
    public SupportedResponse supported() {
        return SupportedResponse.builder()
                // Base networks =======================================================================================
                .kind(SupportedKind.builder()
                        .x402Version(X402_SUPPORTED_VERSION)
                        .scheme(EXACT_SCHEME_NAME)
                        .network(BASE_SEPOLIA.name())
                        .build())
                .build();
    }

}
