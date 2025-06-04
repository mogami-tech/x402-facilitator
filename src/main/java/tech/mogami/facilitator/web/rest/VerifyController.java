package tech.mogami.facilitator.web.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.api.facilitator.verify.VerifyResponse;

import static tech.mogami.commons.api.facilitator.FacilitatorRoutes.VERIFY_URL;

/**
 * /verify endpoint - Verify a payment with a supported scheme and network.
 */
@RestController
public class VerifyController {

    /**
     * Verify a payment request.
     *
     * @param verifyRequest the request containing the payment details to verify
     * @return VerifyResponse containing the verification result
     */
    @GetMapping(VERIFY_URL)
    VerifyResponse verify(final VerifyRequest verifyRequest) {
        return null;
    }

}
