package tech.mogami.facilitator.verifier.general;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
import tech.mogami.commons.payment.PaymentRequirements;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.Verifier;

import static tech.mogami.commons.constant.X402Error.INVALID_PAYMENT_REQUIREMENTS;
import static tech.mogami.facilitator.verifier.VerificationStep.PAYMENT_REQUIREMENTS_MATCH;

/**
 * Verifier to check if the payment requirements are the same as in the accepts field.
 */
@Order(3)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class PaymentRequirementsVerifier implements Verifier {

    @Override
    public VerificationResult verify(final VerificationRequest verifyRequest) {
        final PaymentRequirements paymentRequirements = verifyRequest.paymentRequirements();
        final PaymentRequirements accepted = verifyRequest.paymentPayload().accepted();

        // We check if the payment requirements are compatible with the accepted payment requirements
        if (paymentRequirements.isCompatibleWith(accepted)) {
            return VerificationResult.success();
        } else {
            return VerificationResult.failure(
                    INVALID_PAYMENT_REQUIREMENTS,
                    "Payment requirements do not match the accepted payment requirements");
        }
    }

    @Override
    public VerificationStep type() {
        return PAYMENT_REQUIREMENTS_MATCH;
    }

}
