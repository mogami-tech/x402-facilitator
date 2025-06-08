package tech.mogami.facilitator.verifier.general;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.PaymentPayload;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.Verifier;
import tech.mogami.facilitator.verifier.VerifierUtil;

import java.util.Set;

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

        // Checking the values of field in payment payload in verifyRequest.
        Set<ConstraintViolation<PaymentPayload>> paymentPayloadErrors = validator.validate(verifyRequest.paymentPayload());
        if (paymentPayloadErrors.stream().anyMatch(v -> v.getPropertyPath().toString().equals("scheme"))) {
            return VerificationResult.fail(UNSUPPORTED_SCHEME, getErrorMessage(paymentPayloadErrors.iterator().next()));
        }
        // Checking the values of field in payment requirements in verifyRequest.
        Set<ConstraintViolation<PaymentRequirements>> paymentRequirementsErrors = validator.validate(verifyRequest.paymentRequirements());
        if (paymentRequirementsErrors.stream().anyMatch(v -> v.getPropertyPath().toString().equals("scheme"))) {
            return VerificationResult.fail(UNSUPPORTED_SCHEME, getErrorMessage(paymentRequirementsErrors.iterator().next()));
        }

        return VerificationResult.ok();
    }

    @Override
    public VerificationStep type() {
        return SCHEME_EXISTS;
    }

}
