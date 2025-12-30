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
import tech.mogami.commons.api.facilitator.settle.SettlementRequest;
import tech.mogami.commons.api.facilitator.settle.SettlementResponse;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
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
     * @param settlementRequest the request containing the payment details to settle
     * @return VerifyResponse containing the settlement result
     */
    @PostMapping(SETTLE_ENDPOINT)
    @Operation(summary = "Settle a payment request")
    SettlementResponse settle(@RequestBody final SettlementRequest settlementRequest) {
        final String nonce = settlementRequest.getPaymentId().orElseThrow(() -> new IllegalArgumentException("Nonce is required in the payment payload"));
        final String payer = settlementRequest.getFrom().orElse("PAYER_NOT_FOUND");
        final Optional<Network> network = settlementRequest.getNetwork();
        String errorCode = null;
        String errorMessage = null;
        SettlementResponse settlementResponse = null;

        try {
            // The settle response should reply with the network.
            // TODO Transform as a verifier?
            if (network.isEmpty()) {
                log.error("Unsupported network");
                errorCode = INVALID_NETWORK.getCode();
                errorMessage = INVALID_NETWORK.getDefaultMessage();
                settlementResponse = SettlementResponse.builder()
                        .success(false)
                        .network(null)
                        .errorReason(INVALID_NETWORK.getCode())
                        .payer(payer)
                        .build();
                return settlementResponse;
            }

            // We do the verification again ============================================================================
            log.info("Received settlement request: {}", settlementRequest);
            VerificationResult verificationResult = verifierService
                    .verify(VerificationRequest.builder()
                            .paymentPayload(settlementRequest.paymentPayload())
                            .paymentRequirements(settlementRequest.paymentRequirements())
                            .build());
            if (!verificationResult.isValid()) {
                log.error("Invalid payment request: {}", verificationResult);
                errorCode = verificationResult.verificationError().getCode();
                errorMessage = verificationResult.errorMessage();
                settlementResponse = SettlementResponse.builder()
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
                            settlementRequest.paymentRequirements().asset(),
                            web3j,
                            new RawTransactionManager(web3j,
                                    Credentials.create(x402Parameters.facilitator().privateKey()),
                                    network.get().chainId()),
                            gasService.getGasProvider(network.get())
                    );

                    // We send the transaction using the authorization =================================================
                    log.info("Settling request {} with contract {}",
                            settlementRequest,
                            settlementRequest.paymentRequirements().asset());
                    ExactSchemePayload payload = (ExactSchemePayload) settlementRequest.paymentPayload().payload();
                    var transactionReceipt = contract.transferWithAuthorization(
                                    payload.authorization().from(),
                                    settlementRequest.paymentRequirements().payTo(),
                                    new BigInteger(settlementRequest.paymentRequirements().amount()),
                                    new BigInteger(payload.authorization().validAfter()),
                                    new BigInteger(payload.authorization().validBefore()),
                                    Numeric.hexStringToByteArray(payload.authorization().nonce()),
                                    Numeric.hexStringToByteArray(payload.signature()))
                            .send();

                    // We treat the result of the transaction ==========================================================
                    if (transactionReceipt.isStatusOK()) {
                        log.info("Successfully settled of request {}: {}",
                                settlementRequest,
                                transactionReceipt.getTransactionHash());

                        settlementResponse = SettlementResponse.builder()
                                .success(true)
                                .network(settlementRequest.paymentRequirements().network())
                                .transaction(transactionReceipt.getTransactionHash())
                                .payer(payer)
                                .build();
                    } else {
                        log.error("Failed to settle request {}: {}",
                                settlementRequest,
                                transactionReceipt.getStatus());
                        errorCode = INVALID_TRANSACTION_STATE.getCode();
                        errorMessage = transactionReceipt.getStatus();
                        settlementResponse = SettlementResponse.builder()
                                .success(false)
                                .network(settlementRequest.paymentRequirements().network())
                                .errorReason(errorCode)
                                .payer(payer)
                                .build();
                    }
                } catch (Exception e) {
                    log.error("Exception during request settlement {}: {}",
                            settlementRequest,
                            e.getMessage());
                    errorCode = UNEXPECTED_SETTLE_ERROR.getCode();
                    errorMessage = e.getMessage();
                    settlementResponse = SettlementResponse.builder()
                            .success(false)
                            .network(network.get().name())
                            .errorReason(errorCode)
                            .payer(payer)
                            .build();
                }
            }
            return settlementResponse;
        } finally {
            outboxService.publish(
                    NewPaymentStepMessage.builder()
                            .paymentId(nonce)
                            .paymentStepType(SETTLE)
                            .requestPayload(JsonUtil.toJson(settlementRequest))
                            .responsePayload(JsonUtil.toJson(settlementResponse))
                            .errorCode(errorCode)
                            .errorMessage(errorMessage)
                            .build()
            );
        }
    }

}
