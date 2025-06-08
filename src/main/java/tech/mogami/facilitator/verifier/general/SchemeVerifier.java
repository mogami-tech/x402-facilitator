package tech.mogami.facilitator.verifier.general;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.schemes.Schemes;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.Verifier;

import static tech.mogami.facilitator.verifier.VerificationError.UNSUPPORTED_SCHEME;
import static tech.mogami.facilitator.verifier.VerificationStep.SCHEME_EXISTS;

/**
 * Verifier for schemes.
 */
@Order(2)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class SchemeVerifier implements Verifier {

    @Override
    public VerificationResult verify(final VerifyRequest verifyRequest) {
        // Thanks to RequestNotEmptyVerifier, we know paymentPayload() and paymentRequirements() are not null.
        // So we can safely retrieve them directly from VerifyRequest.
        final String payloadScheme = verifyRequest.paymentPayload().scheme();
        final String paymentScheme = verifyRequest.paymentRequirements().scheme();
        if (StringUtils.isEmpty(payloadScheme)) {
            return VerificationResult.fail(UNSUPPORTED_SCHEME, "Payload scheme is not set");
        }
        if (Schemes.findByName(payloadScheme).isEmpty()) {
            return VerificationResult.fail(UNSUPPORTED_SCHEME, "Payload scheme is invalid: " + payloadScheme);
        }
        if (StringUtils.isEmpty(paymentScheme)) {
            return VerificationResult.fail(UNSUPPORTED_SCHEME, "Payment scheme is not set");
        }
        if (Schemes.findByName(paymentScheme).isEmpty()) {
            return VerificationResult.fail(UNSUPPORTED_SCHEME, "Payment scheme is invalid: " + paymentScheme);
        }
        return VerificationResult.ok();
    }

    @Override
    public VerificationStep type() {
        return SCHEME_EXISTS;
    }

}
