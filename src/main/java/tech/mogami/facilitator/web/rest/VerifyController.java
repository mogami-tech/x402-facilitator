package tech.mogami.facilitator.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.commons.api.console.v1.EventRequest;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.api.facilitator.verify.VerifyResponse;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.provider.console.ConsoleService;
import tech.mogami.facilitator.service.VerifyService;

import static tech.mogami.commons.api.console.EventType.X402_FACILITATOR_VERIFY_REQUEST;
import static tech.mogami.commons.api.console.EventType.X402_FACILITATOR_VERIFY_RESPONSE;
import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.VERIFY_URL;


/**
 * /verify endpoint - Verify a payment.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Verify", description = "Verify a payment")
public class VerifyController {

    /** Console service. */
    private final ConsoleService consoleService;

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
        final String nonce = verifyRequest.getNonce()
                .orElseThrow(() -> new IllegalArgumentException("Nonce is required in the payment payload"));

        // Send X402_FACILITATOR_VERIFY_REQUEST event to console.
        consoleService.logEvent(EventRequest.builder()
                .type(X402_FACILITATOR_VERIFY_REQUEST)
                .nonce(nonce)
                .payload(JsonUtil.toPrettyJson(verifyRequest))
                .build());

        // Call the verification service to process the request.
        VerifyResponse result = verifierService.verify(verifyRequest);

        // X402 Console - Sending X402_FACILITATOR_VERIFY_RESPONSE event to console.
        consoleService.logEvent(EventRequest.builder()
                .type(X402_FACILITATOR_VERIFY_RESPONSE)
                .nonce(nonce)
                .payload(JsonUtil.toPrettyJson(result))
                .errorMessage(result.invalidReason())
                .build());

        log.info("Received verification request: {}", verifyRequest);
        return result;
    }

}
