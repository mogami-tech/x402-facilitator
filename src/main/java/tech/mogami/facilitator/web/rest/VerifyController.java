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
import tech.mogami.facilitator.service.facilitator.VerifyService;
import tech.mogami.facilitator.verifier.VerificationResult;

import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.VERIFY_ENDPOINT;


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
    @PostMapping(VERIFY_ENDPOINT)
    @Operation(summary = "Verify a payment request")
    VerifyResponse verify(@RequestBody final VerifyRequest verifyRequest) {
        final String nonce = verifyRequest.getNonce()
                .orElseThrow(() -> new IllegalArgumentException("Nonce is required in the payment payload"));
        final String payer = verifyRequest.getFromAddress().orElse("PAYER_NOT_FOUND");

        try {
            // Call the verification service to process the request.
            VerificationResult verificationResult = verifierService.verify(verifyRequest);
            if (!verificationResult.isValid()) {
                log.info("Verification error {}", verificationResult.errorMessage());
                return VerifyResponse.builder()
                        .isValid(false)
                        .invalidReason(verificationResult.verificationError().getCode())
                        .payer(payer)
                        .build();
            } else {
                log.info("All verifiers passed for request: {}", verifyRequest);
                return VerifyResponse.builder()
                        .isValid(true)
                        .payer(payer)
                        .build();
            }
        } finally {
            log.info("Verification completed for nonce: {} by payer: {}", nonce, payer);
        }
    }

}
