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

import static tech.mogami.commons.api.facilitator.VerificationError.UNDEFINED;
import static tech.mogami.facilitator.verifier.VerificationStep.REQUEST_NOT_EMPTY;

/**
 * Verifier for requests that ensures they are not empty.
 */
@Order(1)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class RequestNotEmptyVerifier extends VerifierUtil implements Verifier {

    /** Validator. */
    private final Validator validator;

    @Override
    public VerificationResult verify(final VerifyRequest verifyRequest) {
        if (verifyRequest == null) {
            return VerificationResult.fail(UNDEFINED, "The request object received is null");
        }

        return Stream.of(
                        validator.validateProperty(verifyRequest, "paymentPayload"),
                        validator.validateProperty(verifyRequest, "paymentRequirements")
                )
                .flatMap(Set::stream)
                .findFirst()
                .map(violation -> VerificationResult.fail(UNDEFINED, getErrorMessage(violation)))
                .orElseGet(VerificationResult::ok);
    }

    @Override
    public VerificationStep type() {
        return REQUEST_NOT_EMPTY;
    }

}
