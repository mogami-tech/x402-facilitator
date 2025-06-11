package tech.mogami.facilitator.verifier.exact;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.VerifierForExactScheme;

import static tech.mogami.facilitator.verifier.VerificationError.INVALID_EXACT_EVM_PAYLOAD_RECIPIENT_MISMATCH;
import static tech.mogami.facilitator.verifier.VerificationStep.PAYMENT_ADDRESS_FOR_EXACT_SCHEME;

/**
 * Payment address verifier.
 */
@Order(12)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class PaymentAddressVerifier implements VerifierForExactScheme {

    @Override
    public VerificationResult verify(final VerifyRequest verifyRequest) {
        // Verify that payment was made to the correct address
        ExactSchemePayload payload = (ExactSchemePayload) verifyRequest.paymentPayload().payload();

        // Check if the payment address is valid
        if (!StringUtils.equalsIgnoreCase(payload.authorization().to(), verifyRequest.paymentRequirements().payTo())) {
            return VerificationResult.fail(INVALID_EXACT_EVM_PAYLOAD_RECIPIENT_MISMATCH, "Authorization 'to' address does not match the payment requirements 'payTo' address");
        }

        // If everything is fine, we return an OK result
        return VerificationResult.ok();
    }

    @Override
    public VerificationStep type() {
        return PAYMENT_ADDRESS_FOR_EXACT_SCHEME;
    }
}

