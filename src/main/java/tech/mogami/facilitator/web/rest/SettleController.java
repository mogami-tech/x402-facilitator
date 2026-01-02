package tech.mogami.facilitator.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.mogami.commons.api.facilitator.settle.SettlementRequest;
import tech.mogami.commons.api.facilitator.settle.SettlementResponse;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.outbox.NewPaymentStepMessage;
import tech.mogami.facilitator.provider.outbox.service.OutboxService;
import tech.mogami.facilitator.provider.web3j.ContractCallResult;
import tech.mogami.facilitator.provider.web3j.ContractService;
import tech.mogami.facilitator.service.facilitator.VerifyService;
import tech.mogami.facilitator.verifier.VerificationResult;

import java.util.Optional;

import static tech.mogami.commons.api.facilitator.FacilitatorApiEndpoints.SETTLE_ENDPOINT;
import static tech.mogami.commons.constant.X402Error.INVALID_NETWORK;
import static tech.mogami.commons.constant.X402Error.INVALID_TRANSACTION_STATE;
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

    /** Verify service to handle verification logic. */
    private final VerifyService verifierService;

    /** Contract service to handle contract interactions. */
    private final ContractService contractService;

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
                        .network(network.get().networkId())
                        .errorReason(verificationResult.verificationError().getCode())
                        .payer(payer)
                        .build();
            } else {
                // We proceed with the settlement ======================================================================
                final ContractCallResult contractCallResult = contractService.transferWithAuthorization(settlementRequest);
                if (contractCallResult.success()) {
                    settlementResponse = SettlementResponse.builder()
                            .success(true)
                            .network(network.get().networkId())
                            .transaction(contractCallResult.transactionHash())
                            .payer(payer)
                            .build();
                } else {
                    errorCode = INVALID_TRANSACTION_STATE.getCode();
                    errorMessage = contractCallResult.errorMessage();
                    settlementResponse = SettlementResponse.builder()
                            .success(false)
                            .network(network.get().networkId())
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
