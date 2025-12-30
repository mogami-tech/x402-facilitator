package tech.mogami.facilitator.verifier.exact;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.payment.PaymentRequirements;
import tech.mogami.commons.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.VerifierForExactScheme;
import tech.mogami.facilitator.verifier.VerifierUtil;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static tech.mogami.commons.constant.X402Error.INVALID_NETWORK;
import static tech.mogami.commons.constant.network.Networks.findByName;
import static tech.mogami.commons.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_NAME;
import static tech.mogami.commons.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_VERSION;
import static tech.mogami.facilitator.verifier.VerificationStep.PAYMENT_CONTEXT_FOR_EXACT_SCHEME;

/**
 * Payment context verifier.
 */
@Order(10)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber", "checkstyle:MethodLength"})
public class PaymentContextVerifier extends VerifierUtil implements VerifierForExactScheme {

    /** Validator. */
    private final Validator validator;

    @Override
    public VerificationResult verify(final VerificationRequest verificationRequest) {

        // Check the networks ==========================================================================================
        var networkErrors = Stream.of(
                        validator.validateProperty(verificationRequest, "paymentPayload.network"),
                        validator.validateProperty(verificationRequest, "paymentRequirements.network")
                )
                .flatMap(Set::stream)
                .findFirst();
        if (networkErrors.isPresent()) {
            return VerificationResult.fail(INVALID_NETWORK, getErrorMessage(networkErrors.get()));
        }

        // Getting the payload from payment payload ====================================================================
        if (verificationRequest.paymentPayload().payload() == null) {
            return VerificationResult.fail(INVALID_NETWORK, "Payment payload is empty");
        }
        if (!(verificationRequest.paymentPayload().payload() instanceof ExactSchemePayload)) {
            return VerificationResult.fail(INVALID_NETWORK, "Payment payload is not valid (Not an ExactSchemePayload)");
        }

        // Check the stablecoin name to use ============================================================================
        PaymentRequirements paymentRequirements = verificationRequest.paymentRequirements();
        Optional<String> stableCoinName = paymentRequirements.getExtra(EXACT_SCHEME_PARAMETER_NAME);
        if (stableCoinName.isEmpty()) {
            return VerificationResult.fail(INVALID_NETWORK, "Stablecoin name is not provided in the payment requirements");
        } else {
            Optional<Network> network = findByName(verificationRequest.paymentRequirements().network());

            if (network.isEmpty()) {
                return VerificationResult.fail(INVALID_NETWORK, "Unknown network: " + verificationRequest.paymentRequirements().network());
            }
            // If it's not a valid value
            if (!StringUtils.equalsAnyIgnoreCase(stableCoinName.get(), "USDC", "USD Coin")) {
                return VerificationResult.fail(INVALID_NETWORK, "Exact scheme parameter name invalid: " + stableCoinName.get());
            }
            // Checking specific network requirements
            if (StringUtils.equalsIgnoreCase(verificationRequest.paymentRequirements().network(), network.get().name())
                    && !StringUtils.equalsIgnoreCase(stableCoinName.get(), network.get().usdc().displayName())) {
                return VerificationResult.fail(INVALID_NETWORK, "On " + network.get().name() + ", the exact scheme parameter name must be '" + network.get().usdc().displayName() + "'");
            }
        }

        // Check the exact scheme version ==============================================================================
        Optional<String> version = paymentRequirements.getExtra(EXACT_SCHEME_PARAMETER_VERSION);
        if (version.isEmpty()) {
            return VerificationResult.fail(INVALID_NETWORK, "Exact scheme version is not provided in the payment requirements");
        }

        // Check the asset contract address ============================================================================
        var assetErrors = validator.validateProperty(verificationRequest, "paymentRequirements.asset")
                .stream()
                .findFirst();
        return assetErrors.map(verifyRequestConstraintViolation -> VerificationResult.fail(INVALID_NETWORK, getErrorMessage(verifyRequestConstraintViolation))).orElseGet(VerificationResult::ok);
    }

    @Override
    public VerificationStep type() {
        return PAYMENT_CONTEXT_FOR_EXACT_SCHEME;
    }

}
