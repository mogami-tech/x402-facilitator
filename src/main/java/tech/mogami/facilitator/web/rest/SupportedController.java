package tech.mogami.facilitator.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.commons.api.facilitator.supported.SupportedResponse;
import tech.mogami.facilitator.service.SupportedService;

import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.SUPPORTED_ENDPOINT;

/**
 * /supported endpoint - Get supported payment schemes and networks.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Supported", description = "Get supported payment schemes and networks")
public class SupportedController {

    /** Supported service. */
    private final SupportedService supportedService;

    /**
     * Returns supported payment schemes and networks.
     *
     * @return SupportedResponse
     */
    @GetMapping(SUPPORTED_ENDPOINT)
    @Operation(summary = "Supported Payment Schemes and Networks")
    public SupportedResponse supported() {
        return supportedService.supported();
    }

}
