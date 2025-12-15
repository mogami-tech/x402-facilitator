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
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Numeric;
import tech.mogami.commons.api.facilitator.settle.SettleRequest;
import tech.mogami.commons.api.facilitator.settle.SettleResponse;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.crypto.contract.FiatTokenV2_2;
import tech.mogami.commons.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.outbox.NewPaymentStepMessage;
import tech.mogami.facilitator.parameter.X402Parameters;
import tech.mogami.facilitator.provider.outbox.service.OutboxService;
import tech.mogami.facilitator.provider.web3j.GasService;
import tech.mogami.facilitator.service.facilitator.VerifyService;
import tech.mogami.facilitator.verifier.VerificationResult;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;

import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.SETTLE_ENDPOINT;
import static tech.mogami.commons.constant.X402Error.INVALID_NETWORK;
import static tech.mogami.commons.constant.X402Error.INVALID_TRANSACTION_STATE;
import static tech.mogami.commons.constant.X402Error.UNEXPECTED_SETTLE_ERROR;
import static tech.mogami.facilitator.domain.payment.PaymentStepType.SETTLE;

/**
 * /settle endpoint - Settle a payment.
 * TODO Create a service to handle the settlement logic.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Settle", description = "Settle a payment")
@SuppressWarnings({"checkstyle:MethodLength"})
public class SettleController {

    /** X402 parameters. */
    private final X402Parameters x402Parameters;

    /** Web3j clients for different networks. */
    private final Map<Network, Web3j> web3jClients;

    /** Verify service to handle verification logic. */
    private final VerifyService verifierService;

    /** Gas service. */
    private final GasService gasService;

    /** Outbox service to publish payment steps. */
    private final OutboxService outboxService;

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
        final String payer = settleRequest.getFromAddress().orElse("PAYER_NOT_FOUND");
        final Optional<Network> network = settleRequest.getNetwork();
        String errorCode = null;
        String errorMessage = null;
        SettleResponse settleResponse = null;

        try {
            // The settle response should reply with the network.
            // TODO Transform as a verifier?
            if (network.isEmpty()) {
                log.error("Unsupported network");
                errorCode = INVALID_NETWORK.getCode();
                errorMessage = INVALID_NETWORK.getDefaultMessage();
                settleResponse = SettleResponse.builder()
                        .success(false)
                        .network(null)
                        .errorReason(INVALID_NETWORK.getCode())
                        .payer(payer)
                        .build();
                return settleResponse;
            }

            // We do the verification again ============================================================================
            log.info("Received settlement request: {}", settleRequest);
            VerificationResult verificationResult = verifierService
                    .verify(VerifyRequest.builder()
                            .x402Version(settleRequest.x402Version())
                            .paymentPayload(settleRequest.paymentPayload())
                            .paymentRequirements(settleRequest.paymentRequirements())
                            .build());
            if (!verificationResult.isValid()) {
                log.error("Invalid payment request: {}", verificationResult);
                errorCode = verificationResult.verificationError().getCode();
                errorMessage = verificationResult.errorMessage();
                settleResponse = SettleResponse.builder()
                        .success(false)
                        .network(network.get().name())
                        .errorReason(verificationResult.verificationError().getCode())
                        .payer(payer)
                        .build();
            } else {
                try {
                    // Loading the contract to use to make the payment =====================================================
                    final Web3j web3j = web3jClients.get(network.get());
                    FiatTokenV2_2 contract = FiatTokenV2_2.load(
                            settleRequest.paymentRequirements().asset(),
                            web3j,
                            new RawTransactionManager(web3j,
                                    Credentials.create(x402Parameters.facilitator().privateKey()),
                                    network.get().chainId()),
                            gasService.getGasProvider(network.get())
                    );

                    // We send the transaction using the authorization =================================================
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

                    // We treat the result of the transaction ==========================================================
                    if (transactionReceipt.isStatusOK()) {
                        log.info("Successfully settled of request {}: {}",
                                settleRequest,
                                transactionReceipt.getTransactionHash());

                        settleResponse = SettleResponse.builder()
                                .success(true)
                                .network(settleRequest.paymentRequirements().network())
                                .transaction(transactionReceipt.getTransactionHash())
                                .payer(payer)
                                .build();
                    } else {
                        log.error("Failed to settle request {}: {}",
                                settleRequest,
                                transactionReceipt.getStatus());
                        errorCode = INVALID_TRANSACTION_STATE.getCode();
                        errorMessage = transactionReceipt.getStatus();
                        settleResponse = SettleResponse.builder()
                                .success(false)
                                .network(settleRequest.paymentRequirements().network())
                                .errorReason(errorCode)
                                .payer(payer)
                                .build();
                    }
                } catch (Exception e) {
                    log.error("Exception during request settlement {}: {}",
                            settleRequest,
                            e.getMessage());
                    errorCode = UNEXPECTED_SETTLE_ERROR.getCode();
                    errorMessage = e.getMessage();
                    settleResponse = SettleResponse.builder()
                            .success(false)
                            .network(network.get().name())
                            .errorReason(errorCode)
                            .payer(payer)
                            .build();
                }
            }
            return settleResponse;
        } finally {
            outboxService.publish(
                    NewPaymentStepMessage.builder()
                            .paymentId(nonce)
                            .paymentStepType(SETTLE)
                            .requestPayload(JsonUtil.toJson(settleRequest))
                            .responsePayload(JsonUtil.toJson(settleResponse))
                            .errorCode(errorCode)
                            .errorMessage(errorMessage)
                            .build()
            );
        }
    }

}
