package tech.mogami.facilitator.verifier.exact;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.crypto.signature.EIP712Helper;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.VerifierForExactScheme;

import static tech.mogami.facilitator.verifier.VerificationError.INVALID_EXACT_SIGNATURE;
import static tech.mogami.facilitator.verifier.VerificationStep.SIGNATURE_FOR_EXACT_SCHEME;

/**
 * Signature verifier.
 */
@Order(11)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class SignatureVerifier implements VerifierForExactScheme {

    @Override
    public VerificationResult verify(final VerifyRequest verifyRequest) {
        try {
            // We retrieve the payload and signature from the request
            ExactSchemePayload payload = (ExactSchemePayload) verifyRequest.paymentPayload().payload();
            if (EIP712Helper.verify(
                    payload.signature(),
                    verifyRequest.paymentRequirements(),
                    verifyRequest.paymentPayload(),
                    payload.authorization().from())) {
                return VerificationResult.ok();
            } else {
                return VerificationResult.fail(INVALID_EXACT_SIGNATURE, "Signature verification failed for exact scheme");
            }
        } catch (Exception e) {
            return VerificationResult.fail(INVALID_EXACT_SIGNATURE, "Signature verification exception: " + e.getMessage());
        }
    }

    @Override
    public VerificationStep type() {
        return SIGNATURE_FOR_EXACT_SCHEME;
    }

}
