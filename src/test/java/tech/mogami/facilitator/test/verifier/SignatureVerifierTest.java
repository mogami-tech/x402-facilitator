package tech.mogami.facilitator.test.verifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.PaymentPayload;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.verifier.exact.SignatureVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.mogami.commons.api.facilitator.VerificationError.INVALID_EXACT_SIGNATURE;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;
import static tech.mogami.commons.header.payment.schemes.Schemes.EXACT_SCHEME;
import static tech.mogami.commons.header.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_NAME;
import static tech.mogami.commons.header.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_VERSION;
import static tech.mogami.commons.test.BaseTestData.TEST_CLIENT_WALLET_ADDRESS_1;
import static tech.mogami.commons.test.BaseTestData.TEST_SERVER_WALLET_ADDRESS_1;

@SpringBootTest
@DisplayName("Signature verifier tests")
public class SignatureVerifierTest {

    @Autowired
    private SignatureVerifier signatureVerifier;

    @Test
    @DisplayName("Invalid signature")
    public void testInvalidSignature() {
        // We change just one parameter, validBefore, to make the signature invalid.
        PaymentRequirements paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.name())
                .maxAmountRequired("10000")
                .resource("http://localhost/weather")
                .payTo(TEST_SERVER_WALLET_ADDRESS_1)
                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                .build();
        PaymentPayload paymentPayload = PaymentPayload.builder()
                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.name())
                .payload(ExactSchemePayload.builder()
                        .signature("0xde533856d81c76984a8dbc8d563bbb6d6d4ca36ce6c4d6e8cf315de3bfc14ab26d6bcdc37549aeed78bf92e39d5180268f8f399a4ffb816cfbf500823882b6001c")
                        .authorization(ExactSchemePayload.Authorization.builder()
                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                .value("10000")
                                .validAfter("1748534647")
                                .validBefore("1748534768")
                                .nonce("0x9b750f5097972d82c02ac371278b83ecf3ca3be8387db59e664eb38c98f97a3d")
                                .build()
                        ).build()
                ).build();

        assertThat(signatureVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(paymentPayload)
                        .paymentRequirements(paymentRequirements)
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_EXACT_SIGNATURE);
                    assertThat(result.errorMessage()).isEqualTo("Signature verification failed for exact scheme");
                });
    }

    @Test
    @DisplayName("Valid signature")
    public void testValidSignature() {
        PaymentRequirements paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.name())
                .maxAmountRequired("10000")
                .resource("http://localhost/weather")
                .payTo(TEST_SERVER_WALLET_ADDRESS_1)
                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                .build();
        PaymentPayload paymentPayload = PaymentPayload.builder()
                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.name())
                .payload(ExactSchemePayload.builder()
                        .signature("0xde533856d81c76984a8dbc8d563bbb6d6d4ca36ce6c4d6e8cf315de3bfc14ab26d6bcdc37549aeed78bf92e39d5180268f8f399a4ffb816cfbf500823882b6001c")
                        .authorization(ExactSchemePayload.Authorization.builder()
                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                .value("10000")
                                .validAfter("1748534647")
                                .validBefore("1748534767")
                                .nonce("0x9b750f5097972d82c02ac371278b83ecf3ca3be8387db59e664eb38c98f97a3d")
                                .build()
                        ).build()
                ).build();

        assertThat(signatureVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(paymentPayload)
                        .paymentRequirements(paymentRequirements)
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isTrue();
                    assertThat(result.verificationError()).isNull();
                    assertThat(result.errorMessage()).isNull();
                });
    }

}
