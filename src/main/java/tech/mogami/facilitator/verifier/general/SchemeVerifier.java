package tech.mogami.facilitator.verifier.general;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.Verifier;
import tech.mogami.facilitator.verifier.VerifierUtil;

import java.util.Set;
import java.util.stream.Stream;

import static tech.mogami.commons.constant.X402Error.UNSUPPORTED_SCHEME;
import static tech.mogami.facilitator.verifier.VerificationStep.SCHEME_EXISTS;

/**
 * Verifier for schemes.
 */
@Order(2)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class SchemeVerifier extends VerifierUtil implements Verifier {

    /** Validator. */
    private final Validator validator;

    @Override
    public VerificationResult verify(final VerificationRequest verificationRequest) {
        return Stream.of(
                        validator.validateProperty(verificationRequest, "paymentPayload.accepted.scheme"),
                        validator.validateProperty(verificationRequest, "paymentRequirements.scheme")
                )
                .flatMap(Set::stream)
                .findFirst()
                .map(violation -> VerificationResult.failure(
                        UNSUPPORTED_SCHEME,
                        getErrorMessage(violation)))
                .orElseGet(VerificationResult::success);
    }

    @Override
    public VerificationStep type() {
        return SCHEME_EXISTS;
    }

}
