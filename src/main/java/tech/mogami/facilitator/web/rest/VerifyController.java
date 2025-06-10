package tech.mogami.facilitator.web.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.api.facilitator.verify.VerifyResponse;
import tech.mogami.facilitator.service.VerifyService;

import static tech.mogami.commons.api.facilitator.FacilitatorRoutes.VERIFY_URL;

/**
 * /verify endpoint - Verify a payment with a supported scheme and network.
 */
@RestController
@RequiredArgsConstructor
public class VerifyController {

    /** Verify service to handle verification logic. */
    private final VerifyService verifierService;

    /**
     * Verify a payment request.
     *
     * @param verifyRequest the request containing the payment details to verify
     * @return VerifyResponse containing the verification result
     */
    @PostMapping(VERIFY_URL)
    VerifyResponse verify(@RequestBody final VerifyRequest verifyRequest) {
        return verifierService.verify(verifyRequest);
    }

}
