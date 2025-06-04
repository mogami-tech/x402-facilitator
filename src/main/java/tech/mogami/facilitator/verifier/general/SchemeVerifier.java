package tech.mogami.facilitator.verifier.general;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.Verifier;

import static tech.mogami.facilitator.verifier.VerificationStep.SCHEME;

/**
 * Verifier for schemes.
 */
@Order(2)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class SchemeVerifier implements Verifier {

    @Override
    public VerificationResult verify(final VerifyRequest verifyRequest) {
        return VerificationResult.ok();
    }

    @Override
    public VerificationStep type() {
        return SCHEME;
    }

}
