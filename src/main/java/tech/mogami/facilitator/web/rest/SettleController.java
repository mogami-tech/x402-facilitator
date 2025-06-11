package tech.mogami.facilitator.web.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.StaticGasProvider;
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

import static tech.mogami.commons.api.facilitator.FacilitatorRoutes.SETTLE_URL;

/**
 * /settle endpoint - Settle a payment.
 */
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
            System.out.println("Verification failed: " + verifyResult.invalidReason());
        }

        try (Web3j web3j = Web3j.build(new HttpService("https://sepolia.base.org"))) {

            FiatTokenV2_2 contract = FiatTokenV2_2.load(
                    verifyRequest.paymentRequirements().asset(),
                    web3j,
                    new RawTransactionManager(web3j,
                            Credentials.create(x402Parameters.facilitator().privateKey()),
                            Long.parseLong(web3j.netVersion().send().getNetVersion())),
                    new StaticGasProvider(
                            Convert.toWei("1", Convert.Unit.GWEI).toBigInteger(),
                            BigInteger.valueOf(300_000))
            );

            ExactSchemePayload payload = (ExactSchemePayload) verifyRequest.paymentPayload().payload();
            var result = contract.transferWithAuthorization(
                    payload.authorization().from(),
                    verifyRequest.paymentRequirements().payTo(),
                    new BigInteger(verifyRequest.paymentRequirements().maxAmountRequired()),
                    new BigInteger(payload.authorization().validAfter()),
                    new BigInteger(payload.authorization().validBefore()),
                    Numeric.hexStringToByteArray(payload.authorization().nonce()),
                    Numeric.hexStringToByteArray(payload.signature())
            ).send();
            System.out.printf("Transaction successful: %s%n", result.getTransactionHash());

        } catch (Exception e) {
            System.out.println("Error getting transaction: " + e.getMessage());
            return SettleResponse.builder().build();
        }

        return SettleResponse.builder()

                .build();

    }

}
