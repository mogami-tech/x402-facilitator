package tech.mogami.facilitator.verifier.exact;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
import tech.mogami.commons.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.VerifierForExactScheme;

import static tech.mogami.commons.constant.X402Error.INVALID_EXACT_EVM_PAYLOAD_AUTHORIZATION_VALID_AFTER;
import static tech.mogami.commons.constant.X402Error.INVALID_EXACT_EVM_PAYLOAD_AUTHORIZATION_VALID_BEFORE;
import static tech.mogami.facilitator.verifier.VerificationStep.DEADLINES_FOR_EXACT_SCHEME;

/**
 * Deadline verifier.
 */
@Order(12)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class DeadlineVerifier implements VerifierForExactScheme {

    @Override
    public VerificationResult verify(final VerificationRequest verifyRequest) {
        ExactSchemePayload payload = (ExactSchemePayload) verifyRequest.paymentPayload().getTypedPayload();
        long currentTimeSeconds = System.currentTimeMillis() / 1000;

        // Check validBefore.
        String validBefore = payload.authorization().validBefore();
        if (validBefore == null || Long.parseLong(validBefore) < (currentTimeSeconds + 6)) {
            return VerificationResult.failure(
                    INVALID_EXACT_EVM_PAYLOAD_AUTHORIZATION_VALID_BEFORE,
                    "Authorization 'validBefore' is in the past or too close to the current time");
        }
        // Check validAfter.
        String validAfter = payload.authorization().validAfter();
        if (validAfter == null || Long.parseLong(validAfter) > currentTimeSeconds) {
            return VerificationResult.failure(
                    INVALID_EXACT_EVM_PAYLOAD_AUTHORIZATION_VALID_AFTER,
                    "Authorization 'validAfter' is in the future");
        }
        return VerificationResult.success();
    }

    @Override
    public VerificationStep type() {
        return DEADLINES_FOR_EXACT_SCHEME;
    }

}
