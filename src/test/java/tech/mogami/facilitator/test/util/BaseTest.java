package tech.mogami.facilitator.test.util;

import org.apache.commons.lang3.StringUtils;
import tech.mogami.commons.api.facilitator.settle.SettlementRequest;
import tech.mogami.commons.api.facilitator.settle.SettlementResponse;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
import tech.mogami.commons.api.facilitator.verify.VerificationResponse;
import tech.mogami.commons.constant.X402Error;
import tech.mogami.commons.constant.network.Network;
import tech.mogami.commons.constant.version.X402Version;
import tech.mogami.commons.payment.PaymentPayload;
import tech.mogami.commons.payment.PaymentRequirements;
import tech.mogami.commons.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.commons.util.JsonUtil;

import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.payment.schemes.Schemes.EXACT_SCHEME;

/**
 * Base class for tests.
 */
public abstract class BaseTest {

    public String normalizeSpaces(String input) {
        return StringUtils.defaultString(input)
                .replace("\u202F", " ")  // Narrow no-break space
                .replace("\u00A0", " ")  // No-break space
                .replace("\u2007", " ")  // Figure space
                .replace("\u2009", " "); // Thin space
    }

    public String getVerificationRequest(
            final X402Version x402Version,
            final Network network,
            final String fromAddress,
            final String toAddress,
            final String assetContract,
            final String amount,
            final String nonce
    ) {
        return JsonUtil.toPrettyJson(VerificationRequest.builder()
//                .x402Version(x402Version.version())
                .paymentPayload(PaymentPayload.builder()
                                .x402Version(x402Version.version())
//                        .scheme(EXACT_SCHEME.name())
//                        .network(network.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("")
                                        .authorization(
                                                ExactSchemePayload.Authorization.builder()
                                                        .from(fromAddress)
                                                        .to(toAddress)
                                                        .value(amount)
                                                        .validAfter("1747601321")
                                                        .validBefore("1747601441")
                                                        .nonce(nonce)
                                                        .build()
                                        )
                                        .build()
                                )
                                .build()
                )
                .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .amount("1000")
//                        .description("")
//                        .mimeType("")
                                .payTo("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                .maxTimeoutSeconds(60)
                                .asset(assetContract)
                                .extra("name", "USDC")
                                .extra("version", "2")
                                .build()
                ).build());
    }

    public String getVerifyResponse(
            final boolean isValid,
            final X402Error invalidReason,
            final String payer
    ) {
        return JsonUtil.toPrettyJson(VerificationResponse.builder()
                .isValid(isValid)
                .invalidReason(invalidReason.getCode())
                .payer(payer)
                .build());
    }

    public String getSettleRequest(
            final X402Version x402Version,
            final Network network,
            final String fromAddress,
            final String toAddress,
            final String assetContract,
            final String amount,
            final String nonce
    ) {
        return JsonUtil.toPrettyJson(SettlementRequest.builder()
//                .x402Version(x402Version.version())
                .paymentPayload(PaymentPayload.builder()
                                .x402Version(x402Version.version())
//                        .scheme(EXACT_SCHEME.name())
//                        .network(network.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("")
                                        .authorization(
                                                ExactSchemePayload.Authorization.builder()
                                                        .from(fromAddress)
                                                        .to(toAddress)
                                                        .value(amount)
                                                        .validAfter("1747601321")
                                                        .validBefore("1747601441")
                                                        .nonce(nonce)
                                                        .build()
                                        )
                                        .build()
                                )
                                .build()
                )
                .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .amount("1000")
//                        .description("")
//                        .mimeType("")
                                .payTo("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                .maxTimeoutSeconds(60)
                                .asset(assetContract)
                                .extra("name", "USDC")
                                .extra("version", "2")
                                .build()
                ).build());
    }

    public String getSettleResponse(
            final boolean success,
            final X402Error errorReason,
            final String transaction,
            final String payer
    ) {
        return JsonUtil.toPrettyJson(SettlementResponse.builder()
                .success(success)
                .errorReason(errorReason.getCode())
                .transaction(transaction)
                .payer(payer)
                .build());
    }

}
