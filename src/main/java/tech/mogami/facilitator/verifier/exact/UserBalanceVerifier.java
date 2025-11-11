package tech.mogami.facilitator.verifier.exact;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.tx.ClientTransactionManager;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.constant.network.Networks;
import tech.mogami.commons.crypto.contract.ERC20;
import tech.mogami.commons.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.provider.web3j.GasService;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.VerifierForExactScheme;

import java.math.BigInteger;
import java.util.Map;

import static tech.mogami.commons.constant.X402Error.INSUFFICIENT_FUNDS;
import static tech.mogami.facilitator.verifier.VerificationStep.USER_BALANCE_FOR_EXACT_SCHEME;

/**
 * User balance verifier.
 * This verifier checks if the user has enough to perform the transaction.
 */
@Order(14)
@Component
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused", "checkstyle:MagicNumber"})
public class UserBalanceVerifier implements VerifierForExactScheme {

    /** Web3j clients for different networks. */
    private final Map<Network, Web3j> web3jClients;

    /** Gas service. */
    private final GasService gasService;

    @Override
    public VerificationResult verify(final VerifyRequest verifyRequest) {
        Network network = Networks.findByName(verifyRequest.paymentRequirements().network())
                .orElseThrow(() -> new IllegalArgumentException("Unsupported network: " + verifyRequest.paymentRequirements().network()));

        try {
            final Web3j web3j = web3jClients.get(network);
            // Retrieve the balance of the user.
            ExactSchemePayload payload = (ExactSchemePayload) verifyRequest.paymentPayload().payload();
            ERC20 token = ERC20.load(
                    verifyRequest.paymentRequirements().asset(),
                    web3j,
                    new ClientTransactionManager(web3j, payload.authorization().from()),
                    gasService.getGasProvider(network)
            );
            // Compare the balance with the required amount.
            BigInteger rawBalance = token.balanceOf(payload.authorization().from()).send();
            if (rawBalance.compareTo(new BigInteger(verifyRequest.paymentRequirements().maxAmountRequired())) < 0) {
                return VerificationResult.fail(
                        INSUFFICIENT_FUNDS,
                        "Insufficient funds: " + rawBalance + " available");
            }
        } catch (Exception e) {
            return VerificationResult.fail(
                    INSUFFICIENT_FUNDS,
                    "Error getting balance: " + e.getMessage());
        }
        return VerificationResult.ok();
    }

    @Override
    public VerificationStep type() {
        return USER_BALANCE_FOR_EXACT_SCHEME;
    }

}
