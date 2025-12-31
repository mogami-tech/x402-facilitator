package tech.mogami.facilitator.verifier.exact;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
import tech.mogami.commons.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.VerifierForExactScheme;

import static tech.mogami.commons.constant.X402Error.INVALID_EXACT_EVM_PAYLOAD_RECIPIENT_MISMATCH;
import static tech.mogami.facilitator.verifier.VerificationStep.PAYMENT_ADDRESS_FOR_EXACT_SCHEME;

/**
 * Payment address verifier.
 */
@Order(11)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class PaymentAddressVerifier implements VerifierForExactScheme {

    @Override
    public VerificationResult verify(final VerificationRequest verificationRequest) {
        // Verify that payment was made to the correct address
        ExactSchemePayload payload = (ExactSchemePayload) verificationRequest.paymentPayload().payload();

        // Check if the payment address is valid
        if (!StringUtils.equalsIgnoreCase(
                payload.authorization().to(),
                verificationRequest.paymentPayload().accepted().payTo())) {
            return VerificationResult.failure(
                    INVALID_EXACT_EVM_PAYLOAD_RECIPIENT_MISMATCH,
                    "Authorization 'to' address does not match the payment requirements 'payTo' address");
        }

        // If everything is fine, we return an OK result
        return VerificationResult.success();
    }

    @Override
    public VerificationStep type() {
        return PAYMENT_ADDRESS_FOR_EXACT_SCHEME;
    }

}

