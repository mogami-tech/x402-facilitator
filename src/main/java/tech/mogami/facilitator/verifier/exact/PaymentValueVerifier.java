package tech.mogami.facilitator.verifier.exact;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.VerifierForExactScheme;

import java.math.BigDecimal;

import static tech.mogami.commons.api.facilitator.VerificationError.INSUFFICIENT_PAYMENT_VALUE;

/**
 * Payment value verifier.
 * Verify value in payload is enough to cover paymentRequirements.maxAmountRequired
 */
@Order(14)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class PaymentValueVerifier implements VerifierForExactScheme {

    @Override
    public VerificationResult verify(final VerifyRequest verifyRequest) {
        // Verify that payment was made to the correct address
        ExactSchemePayload payload = (ExactSchemePayload) verifyRequest.paymentPayload().payload();

        // Check if the payment value is sufficient.
        BigDecimal payloadValue = new BigDecimal(payload.authorization().value());
        BigDecimal maxAmountRequired = new BigDecimal(verifyRequest.paymentRequirements().maxAmountRequired());
        if (payloadValue.compareTo(maxAmountRequired) < 0) {
            return VerificationResult.fail(
                    INSUFFICIENT_PAYMENT_VALUE,
                    "Payment value is less than the required maximum amount (" + payloadValue + " < " + maxAmountRequired + ")"
            );
        }
        return VerificationResult.ok();
    }

    @Override
    public VerificationStep type() {
        return VerificationStep.PAYMENT_VALUE_FOR_EXACT_SCHEME;
    }

}
