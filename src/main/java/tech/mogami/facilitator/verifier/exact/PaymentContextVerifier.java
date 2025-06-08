package tech.mogami.facilitator.verifier.exact;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.constant.networks.Networks;
import tech.mogami.commons.constant.stablecoins.Stablecoins;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.commons.header.payment.schemes.ExactSchemePayload;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.VerifierForExactScheme;

import java.util.Optional;

import static tech.mogami.commons.header.payment.schemes.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_NAME;
import static tech.mogami.facilitator.verifier.VerificationError.INVALID_NETWORK;
import static tech.mogami.facilitator.verifier.VerificationStep.PAYMENT_CONTEXT_FOR_EXACT_SCHEME;

/**
 * Payment context verifier.
 */
@Order(10)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class PaymentContextVerifier implements VerifierForExactScheme {

    @Override
    public VerificationResult verify(final VerifyRequest verifyRequest) {
        // Check the network.
        if (Networks.findByName(verifyRequest.paymentPayload().network()).isEmpty()) {
            return VerificationResult.fail(INVALID_NETWORK, "Network '" + verifyRequest.paymentPayload().network() + "' not supported");
        }

        // Getting the payload from payment payload.
        if (verifyRequest.paymentPayload().payload() == null) {
            return VerificationResult.fail(INVALID_NETWORK, "Payment payload is empty");
        }
        if (!(verifyRequest.paymentPayload().payload() instanceof ExactSchemePayload)) {
            return VerificationResult.fail(INVALID_NETWORK, "Payment payload is not valid");
        }

        // Check the stablecoin name to use.
        PaymentRequirements paymentRequirements = verifyRequest.paymentRequirements();
        Optional<String> stableCoinName = paymentRequirements.getExtra(EXACT_SCHEME_PARAMETER_NAME);
        if (stableCoinName.isEmpty()) {
            return VerificationResult.fail(INVALID_NETWORK, "Stablecoin name is not provided in the payment requirements");
        } else {
            if (Stablecoins.findByName(stableCoinName.get()).isEmpty()) {
                return VerificationResult.fail(INVALID_NETWORK, "Stablecoin name is invalid: " + stableCoinName.get());
            }
        }

        return VerificationResult.ok();
    }

    @Override
    public VerificationStep type() {
        return PAYMENT_CONTEXT_FOR_EXACT_SCHEME;
    }

}
