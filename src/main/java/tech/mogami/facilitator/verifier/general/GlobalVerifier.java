package tech.mogami.facilitator.verifier.general;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
import tech.mogami.commons.constant.X402Error;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.Verifier;
import tech.mogami.facilitator.verifier.VerifierUtil;

import java.util.Comparator;

import static tech.mogami.commons.constant.X402Error.INVALID_PAYLOAD;
import static tech.mogami.commons.constant.X402Error.INVALID_PAYMENT_REQUIREMENTS;
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
                // PaymentPayload fields
                case "paymentPayload" -> 100;
                // PaymentPayload - version field
                case "paymentPayload.x402Version" -> 200;
                // PaymentPayload - PaymentResource fields
                case "paymentPayload.resource" -> 300;
                case "paymentPayload.resource.url" -> 301;
                // PaymentPayload - PaymentRequirements fields
                case "paymentPayload.accepted" -> 400;
                case "paymentPayload.accepted.scheme" -> 401;
                case "paymentPayload.accepted.network" -> 402;
                case "paymentPayload.accepted.amount" -> 403;
                case "paymentPayload.accepted.asset" -> 404;
                case "paymentPayload.accepted.payTo" -> 405;
                case "paymentPayload.accepted.maxTimeoutSeconds" -> 406;
                // PaymentPayload - Payload fields
                case "paymentPayload.payload" -> 500;
                case "paymentPayload.payload.signature" -> 501;
                case "paymentPayload.payload.authorization" -> 502;
                case "paymentPayload.payload.authorization.from" -> 503;
                case "paymentPayload.payload.authorization.to" -> 504;
                case "paymentPayload.payload.authorization.value" -> 505;
                case "paymentPayload.payload.authorization.validAfter" -> 506;
                case "paymentPayload.payload.authorization.validBefore" -> 507;
                case "paymentPayload.payload.authorization.nonce" -> 508;
                // PaymentRequirements fields
                case "paymentRequirements" -> 600;
                case "paymentRequirements.scheme" -> 601;
                case "paymentRequirements.network" -> 602;
                case "paymentRequirements.amount" -> 603;
                case "paymentRequirements.asset" -> 604;
                case "paymentRequirements.payTo" -> 605;
                case "paymentRequirements.maxTimeoutSeconds" -> 606;
                default -> 999;
            }
    );

    /** Validator. */
    private final Validator validator;

    @Override
    public VerificationResult verify(final VerificationRequest verifyRequest) {
        if (verifyRequest == null) {
            return VerificationResult.failure(
                    UNKNOWN,
                    "The request object received is null");
        }

        // Return the first violation found, sorted by property path.
        return validator.validate(verifyRequest).stream()
                .min(VIOLATION_COMPARATOR)
                .map(violation -> {

                    X402Error error = INVALID_PAYLOAD;
                    final String path = violation.getPropertyPath().toString();

                    if (StringUtils.startsWith(path, "paymentPayload.x402Version")) {
                        error = X402Error.INVALID_X402_VERSION;
                    } else if (StringUtils.endsWith(path, ".scheme")) {
                        error = X402Error.INVALID_SCHEME;
                    } else if (StringUtils.endsWith(path, ".network")) {
                        error = X402Error.INVALID_NETWORK;
                    } else if (StringUtils.startsWith(path, "paymentPayload.accepted")) {
                        error = INVALID_PAYMENT_REQUIREMENTS;
                    } else if (StringUtils.startsWith(path, "paymentRequirements")) {
                        error = INVALID_PAYMENT_REQUIREMENTS;
                    }

                    return VerificationResult.failure(
                            error,
                            getErrorMessage(violation));
                })
                .orElseGet(VerificationResult::success);
    }

    @Override
    public VerificationStep type() {
        return GLOBAL_VERIFIER;
    }

}
