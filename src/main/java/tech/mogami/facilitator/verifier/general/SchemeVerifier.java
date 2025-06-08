package tech.mogami.facilitator.verifier.general;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.Verifier;
import tech.mogami.facilitator.verifier.VerifierUtil;

import java.util.Set;
import java.util.stream.Stream;

import static tech.mogami.facilitator.verifier.VerificationError.UNSUPPORTED_SCHEME;
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
    public VerificationResult verify(final VerifyRequest verifyRequest) {
        return Stream.of(
                        validator.validateProperty(verifyRequest, "paymentPayload.scheme"),
                        validator.validateProperty(verifyRequest, "paymentRequirements.scheme")
                )
                .flatMap(Set::stream)
                .findFirst()
                .map(violation -> VerificationResult.fail(UNSUPPORTED_SCHEME, getErrorMessage(violation)))
                .orElseGet(VerificationResult::ok);
    }

    @Override
    public VerificationStep type() {
        return SCHEME_EXISTS;
    }

}
