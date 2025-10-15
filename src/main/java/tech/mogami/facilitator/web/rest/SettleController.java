package tech.mogami.facilitator.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.StaticEIP1559GasProvider;
import org.web3j.utils.Numeric;
import tech.mogami.commons.api.console.v1.EventRequest;
import tech.mogami.commons.api.facilitator.settle.SettleRequest;
import tech.mogami.commons.api.facilitator.settle.SettleResponse;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.api.facilitator.verify.VerifyResponse;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.constant.network.Networks;
import tech.mogami.commons.crypto.contract.FiatTokenV2_2;
import tech.mogami.commons.crypto.gas.GasFees;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.parameter.X402Parameters;
import tech.mogami.facilitator.provider.console.ConsoleService;
import tech.mogami.facilitator.provider.web3j.GasService;
import tech.mogami.facilitator.service.VerifyService;

import java.math.BigInteger;

import static tech.mogami.commons.api.console.EventType.X402_FACILITATOR_SETTLE_REQUEST;
import static tech.mogami.commons.api.console.EventType.X402_FACILITATOR_SETTLE_RESPONSE;
import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.SETTLE_ENDPOINT;
import static tech.mogami.commons.constant.BlockchainConstants.DEFAULT_GAS_LIMIT;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;

/**
 * /settle endpoint - Settle a payment.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Settle", description = "Settle a payment")
@SuppressWarnings({"checkstyle:MethodLength"})
public class SettleController {

    /** X402 parameters. */
    private final X402Parameters x402Parameters;

    /** Console service. */
    private final ConsoleService consoleService;

    /** Verify service to handle verification logic. */
    private final VerifyService verifierService;

    /** Gas service. */
    private final GasService gasService;

    /**
     * Settle a payment request.
     *
     * @param settleRequest the request containing the payment details to settle
     * @return VerifyResponse containing the settlement result
     */
    @PostMapping(SETTLE_ENDPOINT)
    @Operation(summary = "Settle a payment request")
    SettleResponse settle(@RequestBody final SettleRequest settleRequest) {
        final String nonce = settleRequest.getNonce().orElseThrow(() -> new IllegalArgumentException("Nonce is required in the payment payload"));

        // Send X402_FACILITATOR_SETTLE_REQUEST event to console.
        consoleService.logEvent(EventRequest.builder()
                .type(X402_FACILITATOR_SETTLE_REQUEST)
                .nonce(nonce)
                .payload(JsonUtil.toPrettyJson(settleRequest))
                .build());

        log.info("Received settlement request: {}", settleRequest);
        VerifyResponse verifyResult = verifierService
                .verify(VerifyRequest.builder()
                        .x402Version(settleRequest.x402Version())
                        .paymentPayload(settleRequest.paymentPayload())
                        .paymentRequirements(settleRequest.paymentRequirements())
                        .build());

        // The settle response should reply with the network, but we must be sure there is no null value.
        final Network network;
        if (settleRequest.paymentRequirements() == null || settleRequest.paymentRequirements().network() == null) {
            log.error("Payment requirements network is null, using default BASE_SEPOLIA");
            network = BASE_SEPOLIA;
        } else {
            network = Networks.findByName(settleRequest.paymentRequirements().network())
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported network: " + settleRequest.paymentRequirements().network()));
        }

        if (!verifyResult.isValid()) {
            log.error("Invalid payment request: {}", verifyResult);

            SettleResponse response = SettleResponse.builder()
                    .success(false)
                    .network(network.name())
                    .errorReason(verifyResult.invalidReason())
                    .payer(verifyResult.payer())
                    .build();

            // Send X402_FACILITATOR_SETTLE_RESPONSE event to console.
            consoleService.logEvent(EventRequest.builder()
                    .type(X402_FACILITATOR_SETTLE_RESPONSE)
                    .nonce(nonce)
                    .payload(JsonUtil.toPrettyJson(response))
                    .errorMessage(verifyResult.invalidReason())
                    .build());

            return response;
        } else {
            try (Web3j web3j = Web3j.build(new HttpService(network.rpcUrl()))) {
                // Get the gas fees for the network ====================================================================
                GasFees gasFees = gasService.getGasFees(network.name());

                // Loading the contract to use to make the payment =====================================================
                FiatTokenV2_2 contract = FiatTokenV2_2.load(
                        settleRequest.paymentRequirements().asset(),
                        web3j,
                        new RawTransactionManager(web3j,
                                Credentials.create(x402Parameters.facilitator().privateKey()),
                                network.chainId()),
                        new StaticEIP1559GasProvider(
                                network.chainId(),
                                gasFees.maximumFeePerGas(),
                                gasFees.maximumPriorityFeePerGas(),
                                DEFAULT_GAS_LIMIT // gas limit
                        )
                );

                // We send the transaction using the authorization =====================================================
                log.info("Settling request {} with contract {}",
                        settleRequest,
                        settleRequest.paymentRequirements().asset());
                ExactSchemePayload payload = (ExactSchemePayload) settleRequest.paymentPayload().payload();

                var transactionReceipt = contract.transferWithAuthorization(
                                payload.authorization().from(),
                                settleRequest.paymentRequirements().payTo(),
                                new BigInteger(settleRequest.paymentRequirements().maxAmountRequired()),
                                new BigInteger(payload.authorization().validAfter()),
                                new BigInteger(payload.authorization().validBefore()),
                                Numeric.hexStringToByteArray(payload.authorization().nonce()),
                                Numeric.hexStringToByteArray(payload.signature()))
                        .send();

                // We treat the result of the transaction ==============================================================
                if (transactionReceipt.isStatusOK()) {
                    log.info("Successfully settled of request {}: {}",
                            settleRequest,
                            transactionReceipt.getTransactionHash());

                    SettleResponse response = SettleResponse.builder()
                            .success(true)
                            .network(settleRequest.paymentRequirements().network())
                            .transaction(transactionReceipt.getTransactionHash())
                            .payer(verifyResult.payer())
                            .build();

                    // Send X402_FACILITATOR_SETTLE_RESPONSE event to console.
                    consoleService.logEvent(EventRequest.builder()
                            .type(X402_FACILITATOR_SETTLE_RESPONSE)
                            .nonce(nonce)
                            .payload(JsonUtil.toPrettyJson(response))
                            .errorMessage(verifyResult.invalidReason())
                            .build());

                    return response;
                } else {
                    log.error("Failed to settle request {}: {}",
                            settleRequest,
                            transactionReceipt.getStatus());

                    SettleResponse response = SettleResponse.builder()
                            .success(false)
                            .network(settleRequest.paymentRequirements().network())
                            .errorReason("transaction_failed")
                            .payer(verifyResult.payer())
                            .build();

                    // Send X402_FACILITATOR_SETTLE_RESPONSE event to console.
                    consoleService.logEvent(EventRequest.builder()
                            .type(X402_FACILITATOR_SETTLE_RESPONSE)
                            .nonce(nonce)
                            .payload(JsonUtil.toPrettyJson(response))
                            .errorMessage(verifyResult.invalidReason())
                            .build());

                    return response;
                }
            } catch (Exception e) {
                log.error("Exception during request settlement {}: {}",
                        settleRequest,
                        e.getMessage());

                SettleResponse response = SettleResponse.builder()
                        .success(false)
                        .network(network.name())
                        .errorReason(e.getMessage())
                        .payer(verifyResult.payer())
                        .build();

                // Send X402_FACILITATOR_SETTLE_RESPONSE event to console.
                consoleService.logEvent(EventRequest.builder()
                        .type(X402_FACILITATOR_SETTLE_RESPONSE)
                        .nonce(nonce)
                        .payload(JsonUtil.toPrettyJson(response))
                        .errorMessage(verifyResult.invalidReason())
                        .build());

                return response;
            }
        }
    }

}
