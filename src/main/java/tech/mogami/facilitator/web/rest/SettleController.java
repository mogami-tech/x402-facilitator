package tech.mogami.facilitator.web.rest;

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
import tech.mogami.commons.api.facilitator.settle.SettleResponse;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.api.facilitator.verify.VerifyResponse;
import tech.mogami.commons.crypto.contract.FiatTokenV2_2;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
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
public class SettleController {

    /** X402 parameters. */
    private final X402Parameters x402Parameters;

    /** Verify service to handle verification logic. */
    private final VerifyService verifierService;

    /**
     * Settle a payment request.
     *
     * @param verifyRequest the request containing the payment details to settle
     * @return VerifyResponse containing the settlement result
     */
    @PostMapping(SETTLE_URL)
    SettleResponse settle(@RequestBody final VerifyRequest verifyRequest) {
        VerifyResponse verifyResult = verifierService.verify(verifyRequest);
        if (!verifyResult.isValid()) {
            log.error("Invalid payment request: {}", verifyResult);
            return SettleResponse.builder()
                    .success(false)
                    .network(verifyRequest.paymentRequirements().network())
                    .errorReason(verifyResult.invalidReason())
                    .payer(verifyResult.payer())
                    .build();
        } else {

            // TODO Make "https://sepolia.base.org" configurable.
            try (Web3j web3j = Web3j.build(new HttpService("https://sepolia.base.org"))) {

                // Loading the contract to use to make the payment.
                FiatTokenV2_2 contract = FiatTokenV2_2.load(
                        verifyRequest.paymentRequirements().asset(),
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

                // We create the transaction using the authorization details from the ExactSchemePayload.
                ExactSchemePayload payload = (ExactSchemePayload) verifyRequest.paymentPayload().payload();
                var transactionReceipt = contract.transferWithAuthorization(
                                payload.authorization().from(),
                                verifyRequest.paymentRequirements().payTo(),
                                new BigInteger(verifyRequest.paymentRequirements().maxAmountRequired()),
                                new BigInteger(payload.authorization().validAfter()),
                                new BigInteger(payload.authorization().validBefore()),
                                Numeric.hexStringToByteArray(payload.authorization().nonce()),
                                Numeric.hexStringToByteArray(payload.signature()))
                        .send();

                // We treat the result of the transaction.
                if (transactionReceipt.isStatusOK()) {
                    log.info("Successfully settled of request {}: {}",
                            verifyRequest,
                            transactionReceipt.getTransactionHash());
                    return SettleResponse.builder()
                            .success(true)
                            .network(verifyRequest.paymentRequirements().network())
                            .transaction(transactionReceipt.getTransactionHash())
                            .payer(verifyResult.payer())
                            .build();
                } else {
                    log.error("Failed to settle request {}: {}",
                            verifyRequest,
                            transactionReceipt.getStatus());
                    return SettleResponse.builder()
                            .success(false)
                            .network(verifyRequest.paymentRequirements().network())
                            .errorReason("transaction_failed")
                            .payer(verifyResult.payer())
                            .build();
                }
            } catch (Exception e) {
                log.error("Exception during request settlement {}: {}",
                        verifyRequest,
                        e.getMessage());
                return SettleResponse.builder()
                        .success(false)
                        .network(verifyRequest.paymentRequirements().network())
                        .errorReason(e.getMessage())
                        .payer(verifyResult.payer())
                        .build();
            }
        }
    }

}
