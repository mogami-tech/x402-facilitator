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
import tech.mogami.facilitator.outbox.NewPaymentStepMessage;
import tech.mogami.facilitator.provider.outbox.service.OutboxService;
import tech.mogami.facilitator.service.facilitator.VerifyService;
import tech.mogami.facilitator.verifier.VerificationResult;

import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.VERIFY_ENDPOINT;
import static tech.mogami.facilitator.domain.payment.PaymentStepType.VERIFY;


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

    /** Outbox service. */
    private final OutboxService outboxService;

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
        String errorCode = null;
        String errorMessage = null;
        VerifyResponse verifyResponse = null;

        try {
            // Call the verification service to process the request.
            VerificationResult verificationResult = verifierService.verify(verifyRequest);
            if (!verificationResult.isValid()) {
                errorCode = verificationResult.verificationError().getCode();
                errorMessage = verificationResult.errorMessage();
                log.info("Verification error {}", verificationResult.errorMessage());
                verifyResponse = VerifyResponse.builder()
                        .isValid(false)
                        .invalidReason(verificationResult.verificationError().getCode())
                        .payer(payer)
                        .build();
            } else {
                log.info("All verifiers passed for request: {}", verifyRequest);
                verifyResponse = VerifyResponse.builder()
                        .isValid(true)
                        .payer(payer)
                        .build();
            }
            return verifyResponse;
        } finally {
            outboxService.publish(
                    NewPaymentStepMessage.builder()
                            .paymentId(nonce)
                            .paymentStepType(VERIFY)
                            .requestPayload(JsonUtil.toJson(verifyRequest))
                            .responsePayload(JsonUtil.toJson(verifyResponse))
                            .errorCode(errorCode)
                            .errorMessage(errorMessage)
                            .build()
            );
        }
    }

}
