package tech.mogami.facilitator.provider.web3j;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Numeric;
import tech.mogami.commons.api.facilitator.settle.SettlementRequest;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.crypto.contract.FiatTokenV2_2;
import tech.mogami.commons.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.parameter.X402Parameters;

import java.math.BigInteger;
import java.util.Map;

/**
 * {@link ContractService} implementation.
 */
@Slf4j
@Service
@Profile("!mockedBlockchain")
@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class ContractServiceImplementation implements ContractService {

    /** X402 parameters. */
    private final X402Parameters x402Parameters;

    /** Web3j clients for different networks. */
    private final Map<Network, Web3j> web3jClients;

    /** Gas service. */
    private final GasService gasService;

    @Override
    public ContractCallResult transferWithAuthorization(final @NonNull SettlementRequest settlementRequest) {
        final Network network = settlementRequest.getNetwork().orElseThrow(() -> new IllegalArgumentException("Network is required"));
        final String asset = settlementRequest.getAssetContract().orElseThrow(() -> new IllegalArgumentException("Asset contract is required"));
        final String from = settlementRequest.getFrom().orElseThrow(() -> new IllegalArgumentException("From address is required"));
        final String to = settlementRequest.getTo().orElseThrow(() -> new IllegalArgumentException("To address is required"));
        final BigInteger amount = settlementRequest.getAssetAmount().orElseThrow(() -> new IllegalArgumentException("Amount is required"));

        try {
            // Loading the contract to use to make the payment =========================================================
            final Web3j web3j = web3jClients.get(network);
            FiatTokenV2_2 contract = FiatTokenV2_2.load(
                    asset,
                    web3j,
                    new RawTransactionManager(web3j,
                            Credentials.create(x402Parameters.facilitator().privateKey()),
                            network.chainId()),
                    gasService.getGasProvider(network)
            );

            // We send the transaction using the authorization =========================================================
            log.info("Settling request {} with contract {}", settlementRequest, asset);
            ExactSchemePayload payload = (ExactSchemePayload) settlementRequest.paymentPayload().getTypedPayload();
            var transactionReceipt = contract.transferWithAuthorization(
                            from,
                            to,
                            amount,
                            new BigInteger(payload.authorization().validAfter()),
                            new BigInteger(payload.authorization().validBefore()),
                            Numeric.hexStringToByteArray(payload.authorization().nonce()),
                            Numeric.hexStringToByteArray(payload.signature()))
                    .send();

            // We treat the result of the transaction ==================================================================
            if (transactionReceipt.isStatusOK()) {
                log.info("Successfully settled of request {}: {}", settlementRequest, transactionReceipt.getTransactionHash());
                return ContractCallResult.success(
                        transactionReceipt.getStatus(),
                        transactionReceipt.getTransactionHash()
                );
            } else {
                log.error("Failed to settle request {}: {}", settlementRequest, transactionReceipt.getStatus());
                return ContractCallResult.failure(
                        transactionReceipt.getStatus()
                );
            }
        } catch (Exception e) {
            return ContractCallResult.failure(e.getMessage());
        }
    }

}
