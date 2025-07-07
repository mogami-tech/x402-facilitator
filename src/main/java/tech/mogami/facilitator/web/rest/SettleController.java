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
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import tech.mogami.commons.api.facilitator.settle.SettleRequest;
import tech.mogami.commons.api.facilitator.settle.SettleResponse;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.api.facilitator.verify.VerifyResponse;
import tech.mogami.commons.crypto.contract.FiatTokenV2_2;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.parameter.X402Parameters;
import tech.mogami.facilitator.service.VerifyService;

import java.math.BigInteger;

import static org.web3j.utils.Convert.Unit.GWEI;
import static tech.mogami.commons.api.facilitator.FacilitatorRoutes.SETTLE_URL;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;

/**
 * /settle endpoint - Settle a payment.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Settle", description = "Settle a payment")
public class SettleController {

    /** X402 parameters. */
    private final X402Parameters x402Parameters;

    /** Verify service to handle verification logic. */
    private final VerifyService verifierService;

    /**
     * Settle a payment request.
     *
     * @param settleRequest the request containing the payment details to settle
     * @return VerifyResponse containing the settlement result
     */
    @PostMapping(SETTLE_URL)
    @Operation(summary = "Settle a payment request")
    SettleResponse settle(@RequestBody final SettleRequest settleRequest) {

        // X402 Console - Sending X402_FACILITATOR_SETTLE_REQUEST event to console.
        log.info("Sending X402_FACILITATOR_SETTLE_REQUEST event to console: {}", JsonUtil.toJson(settleRequest));

        log.info("Received settlement request: {}", settleRequest);
        VerifyResponse verifyResult = verifierService
                .verify(VerifyRequest.builder()
                        .x402Version(settleRequest.x402Version())
                        .paymentPayload(settleRequest.paymentPayload())
                        .paymentRequirements(settleRequest.paymentRequirements())
                        .build());

        if (!verifyResult.isValid()) {
            log.error("Invalid payment request: {}", verifyResult);

            // The settle response should reply with the network, but we must be sure there is no null value.
            String network;
            if (settleRequest.paymentRequirements() == null || settleRequest.paymentRequirements().network() == null) {
                log.error("Payment requirements network is null, using default BASE_SEPOLIA");
                network = BASE_SEPOLIA.name();
            } else {
                network = settleRequest.paymentRequirements().network();
            }

            SettleResponse response = SettleResponse.builder()
                    .success(false)
                    .network(network)
                    .errorReason(verifyResult.invalidReason())
                    .payer(verifyResult.payer())
                    .build();

            // X402 Console - Sending X402_FACILITATOR_SETTLE_ERROR event to console.
            log.error("Sending X402_FACILITATOR_SETTLE_RESPONSE event to console: {}", JsonUtil.toJson(response));

            return response;
        } else {
            // TODO Make "https://sepolia.base.org" configurable.
            try (Web3j web3j = Web3j.build(new HttpService("https://sepolia.base.org"))) {

                // Loading the contract to use to make the payment =====================================================
                FiatTokenV2_2 contract = FiatTokenV2_2.load(
                        settleRequest.paymentRequirements().asset(),
                        web3j,
                        new RawTransactionManager(web3j,
                                Credentials.create(x402Parameters.facilitator().privateKey()),
                                Long.parseLong(web3j.netVersion().send().getNetVersion())),
                        // TODO change this to a more suitable gas provider depending on the network chosen.
                        new StaticEIP1559GasProvider(
                                BASE_SEPOLIA.chainId(),
                                Convert.toWei("0.002", GWEI).toBigInteger(),   // maxFee ≈ 0.002 gwei
                                Convert.toWei("0.001", GWEI).toBigInteger(),   // priority ≈ 0.001 gwei
                                new BigInteger("120000") // gas limit
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

                    // X402 Console - Sending X402_FACILITATOR_SETTLE_ERROR event to console.
                    log.error("Sending X402_FACILITATOR_SETTLE_RESPONSE event to console: {}", JsonUtil.toJson(response));

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

                    // X402 Console - Sending X402_FACILITATOR_SETTLE_ERROR event to console.
                    log.error("Sending X402_FACILITATOR_SETTLE_RESPONSE event to console: {}", JsonUtil.toJson(response));

                    return response;
                }
            } catch (Exception e) {
                log.error("Exception during request settlement {}: {}",
                        settleRequest,
                        e.getMessage());

                SettleResponse response = SettleResponse.builder()
                        .success(false)
                        .network(settleRequest.paymentRequirements().network())
                        .errorReason(e.getMessage())
                        .payer(verifyResult.payer())
                        .build();

                // X402 Console - Sending X402_FACILITATOR_SETTLE_ERROR event to console.
                log.error("Sending X402_FACILITATOR_SETTLE_RESPONSE event to console: {}", JsonUtil.toJson(response));

                return response;
            }
        }
    }

}
