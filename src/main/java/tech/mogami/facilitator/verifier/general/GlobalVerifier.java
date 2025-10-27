package tech.mogami.facilitator.verifier.general;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.Verifier;
import tech.mogami.facilitator.verifier.VerifierUtil;

import java.util.Comparator;

import static tech.mogami.commons.constant.X402Error.INVALID_PAYLOAD;
import static tech.mogami.commons.constant.X402Error.UNKNOWN;
import static tech.mogami.facilitator.verifier.VerificationStep.GLOBAL_VERIFIER;

/**
 * Global verifier (checkin all fields).
 */
@Order(1)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class GlobalVerifier extends VerifierUtil implements Verifier {

    /** Comparator to sort constraint violations by declaration order. */
    private static final Comparator<ConstraintViolation<?>> VIOLATION_COMPARATOR = Comparator.comparingInt(
            constraintViolation -> switch (constraintViolation.getPropertyPath().toString()) {
                case "x402Version" -> 100;
                // PaymentPayload fields
                case "paymentPayload" -> 200;
                case "paymentPayload.x402Version" -> 201;
                case "paymentPayload.scheme" -> 202;
                case "paymentPayload.network" -> 203;
                case "paymentPayload.payload" -> 204;
                case "paymentPayload.payload.signature" -> 205;
                case "paymentPayload.payload.authorization" -> 206;
                case "paymentPayload.payload.authorization.from" -> 207;
                case "paymentPayload.payload.authorization.to" -> 208;
                case "paymentPayload.payload.authorization.value" -> 209;
                case "paymentPayload.payload.authorization.validAfter" -> 210;
                case "paymentPayload.payload.authorization.validBefore" -> 211;
                case "paymentPayload.payload.authorization.nonce" -> 212;
                // PaymentRequirements fields
                case "paymentRequirements" -> 300;
                case "paymentRequirements.scheme" -> 301;
                case "paymentRequirements.network" -> 302;
                case "paymentRequirements.maxAmountRequired" -> 303;
                case "paymentRequirements.resource" -> 304;
                case "paymentRequirements.payTo" -> 305;
                case "paymentRequirements.maxTimeoutSeconds" -> 306;
                case "paymentRequirements.asset" -> 307;
                default -> 999;
            }
    );

    /** Validator. */
    private final Validator validator;

    @Override
    public VerificationResult verify(final VerifyRequest verifyRequest) {
        if (verifyRequest == null) {
            return VerificationResult.fail(
                    UNKNOWN,
                    "The request object received is null");
        }

        // Return the first violation found, sorted by property path.
        return validator.validate(verifyRequest).stream()
                .min(VIOLATION_COMPARATOR)
                .map(violation -> VerificationResult.fail(
                        INVALID_PAYLOAD,
                        getErrorMessage(violation)))
                .orElseGet(VerificationResult::ok);
    }

    @Override
    public VerificationStep type() {
        return GLOBAL_VERIFIER;
    }

}
