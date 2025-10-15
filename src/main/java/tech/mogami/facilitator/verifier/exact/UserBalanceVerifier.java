package tech.mogami.facilitator.verifier.exact;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.gas.StaticEIP1559GasProvider;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.constant.network.Networks;
import tech.mogami.commons.crypto.contract.ERC20;
import tech.mogami.commons.crypto.gas.GasFees;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.provider.web3j.GasService;
import tech.mogami.facilitator.verifier.VerificationResult;
import tech.mogami.facilitator.verifier.VerificationStep;
import tech.mogami.facilitator.verifier.VerifierForExactScheme;

import java.math.BigInteger;

import static tech.mogami.commons.constant.BlockchainConstants.DEFAULT_GAS_LIMIT;
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

    /** Gas service. */
    private final GasService gasService;

    @Override
    public VerificationResult verify(final VerifyRequest verifyRequest) {
        Network network = Networks.findByName(verifyRequest.paymentRequirements().network())
                .orElseThrow(() -> new IllegalArgumentException("Unsupported network: " + verifyRequest.paymentRequirements().network()));

        try (Web3j web3j = Web3j.build(new HttpService(network.rpcUrl()))) {
            // Retrieve the balance of the user.
            ExactSchemePayload payload = (ExactSchemePayload) verifyRequest.paymentPayload().payload();
            GasFees gasFees = gasService.getGasFees(network.name());
            ERC20 token = ERC20.load(
                    verifyRequest.paymentRequirements().asset(),
                    web3j,
                    new ClientTransactionManager(web3j, payload.authorization().from()),
                    new StaticEIP1559GasProvider(
                            network.chainId(),
                            gasFees.maximumFeePerGas(),
                            gasFees.maximumPriorityFeePerGas(),
                            DEFAULT_GAS_LIMIT // gas limit
                    )
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
