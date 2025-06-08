package tech.mogami.facilitator.verifier.general;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.Verifier;

import static tech.mogami.facilitator.verifier.VerificationError.UNDEFINED;
import static tech.mogami.facilitator.verifier.VerificationStep.REQUEST_NOT_EMPTY;

/**
 * Verifier for requests that ensures they are not empty.
 */
@Order(1)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class RequestNotEmptyVerifier implements Verifier {

    @Override
    public VerificationResult verify(final VerifyRequest verifyRequest) {
        if (verifyRequest == null) {
            return VerificationResult.fail(UNDEFINED, "The request object received is null");
        }
        if (verifyRequest.paymentPayload() == null) {
            return VerificationResult.fail(UNDEFINED, "The payment payload in the request is null");
        }
        if (verifyRequest.paymentRequirements() == null) {
            return VerificationResult.fail(UNDEFINED, "The payment requirements in the request is null");
        }
        return VerificationResult.ok();
    }

    @Override
    public VerificationStep type() {
        return REQUEST_NOT_EMPTY;
    }

}
