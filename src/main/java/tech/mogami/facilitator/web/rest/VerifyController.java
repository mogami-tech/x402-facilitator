package tech.mogami.facilitator.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.api.facilitator.verify.VerifyResponse;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.service.VerifyService;

import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.VERIFY_URL;


/**
 * /verify endpoint - Verify a payment.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Verify", description = "Verify a payment")
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
    @Operation(summary = "Verify a payment request")
    VerifyResponse verify(@RequestBody final VerifyRequest verifyRequest) {
        // X402 Console - Sending X402_FACILITATOR_VERIFY_REQUEST event to console.
        log.info("Sending X402_FACILITATOR_VERIFY_REQUEST event to console: {}", JsonUtil.toJson(verifyRequest));

        VerifyResponse result = verifierService.verify(verifyRequest);

        // X402 Console - Sending X402_FACILITATOR_VERIFY_RESPONSE event to console.
        log.info("Sending X402_FACILITATOR_VERIFY_RESPONSE event to console: {}", JsonUtil.toJson(result));

        log.info("Received verification request: {}", verifyRequest);
        return result;
    }

}
