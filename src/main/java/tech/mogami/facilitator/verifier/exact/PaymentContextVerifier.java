package tech.mogami.facilitator.verifier.exact;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.constant.stablecoin.Stablecoins;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.VerifierForExactScheme;
import tech.mogami.facilitator.verifier.VerifierUtil;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static tech.mogami.commons.api.facilitator.VerificationError.INVALID_NETWORK;
import static tech.mogami.commons.header.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_NAME;
import static tech.mogami.commons.header.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_VERSION;
import static tech.mogami.facilitator.verifier.VerificationStep.PAYMENT_CONTEXT_FOR_EXACT_SCHEME;

/**
 * Payment context verifier.
 */
@Order(10)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class PaymentContextVerifier extends VerifierUtil implements VerifierForExactScheme {

    /** Validator. */
    private final Validator validator;

    @Override
    public VerificationResult verify(final VerifyRequest verifyRequest) {

        // Check the networks ==========================================================================================
        var networkErrors = Stream.of(
                        validator.validateProperty(verifyRequest, "paymentPayload.network"),
                        validator.validateProperty(verifyRequest, "paymentRequirements.network")
                )
                .flatMap(Set::stream)
                .findFirst();
        if (networkErrors.isPresent()) {
            return VerificationResult.fail(INVALID_NETWORK, getErrorMessage(networkErrors.get()));
        }

        // Getting the payload from payment payload ====================================================================
        if (verifyRequest.paymentPayload().payload() == null) {
            return VerificationResult.fail(INVALID_NETWORK, "Payment payload is empty");
        }
        if (!(verifyRequest.paymentPayload().payload() instanceof ExactSchemePayload)) {
            return VerificationResult.fail(INVALID_NETWORK, "Payment payload is not valid (Not an ExactSchemePayload)");
        }

        // Check the stablecoin name to use ============================================================================
        PaymentRequirements paymentRequirements = verifyRequest.paymentRequirements();
        Optional<String> stableCoinName = paymentRequirements.getExtra(EXACT_SCHEME_PARAMETER_NAME);
        if (stableCoinName.isEmpty()) {
            return VerificationResult.fail(INVALID_NETWORK, "Stablecoin name is not provided in the payment requirements");
        } else {
            if (Stablecoins.findByName(stableCoinName.get()).isEmpty()) {
                return VerificationResult.fail(INVALID_NETWORK, "Stablecoin name is invalid: " + stableCoinName.get());
            }
        }

        // Check the exact scheme version ==============================================================================
        Optional<String> version = paymentRequirements.getExtra(EXACT_SCHEME_PARAMETER_VERSION);
        if (version.isEmpty()) {
            return VerificationResult.fail(INVALID_NETWORK, "Exact scheme version is not provided in the payment requirements");
        }

        // Check the asset contract address ============================================================================
        var assetErrors = validator.validateProperty(verifyRequest, "paymentRequirements.asset")
                .stream()
                .findFirst();
        if (assetErrors.isPresent()) {
            return VerificationResult.fail(INVALID_NETWORK, getErrorMessage(assetErrors.get()));
        }

        return VerificationResult.ok();
    }

    @Override
    public VerificationStep type() {
        return PAYMENT_CONTEXT_FOR_EXACT_SCHEME;
    }

}
