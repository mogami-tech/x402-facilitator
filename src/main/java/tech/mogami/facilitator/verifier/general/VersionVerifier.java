package tech.mogami.facilitator.verifier.general;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
import tech.mogami.commons.constant.version.X402Versions;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.Verifier;
import tech.mogami.facilitator.verifier.VerifierUtil;

import static tech.mogami.commons.constant.X402Error.INVALID_X402_VERSION;
import static tech.mogami.facilitator.verifier.VerificationStep.X402_VERSION_SUPPORTED;

/**
 * Verifier to check if the x402 version is supported.
 */
@Order(2)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class VersionVerifier extends VerifierUtil implements Verifier {

    @Override
    public VerificationResult verify(final VerificationRequest verifyRequest) {
        return X402Versions.findByVersion(verifyRequest.paymentPayload().x402Version())
                .map(v -> VerificationResult.success())
                .orElseGet(() -> VerificationResult.failure(
                        INVALID_X402_VERSION,
                        "x402 version " + verifyRequest.paymentPayload().x402Version() + " not supported"));
    }

    @Override
    public VerificationStep type() {
        return X402_VERSION_SUPPORTED;
    }

}
