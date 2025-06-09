package tech.mogami.facilitator.test.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.PaymentPayload;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.commons.header.payment.schemes.ExactSchemePayload;
import tech.mogami.facilitator.service.VerifyService;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;
import static tech.mogami.commons.header.payment.schemes.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_NAME;
import static tech.mogami.commons.header.payment.schemes.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_VERSION;
import static tech.mogami.commons.header.payment.schemes.Schemes.EXACT_SCHEME;
import static tech.mogami.commons.test.BaseTestData.TEST_CLIENT_WALLET_ADDRESS_1;
import static tech.mogami.commons.test.BaseTestData.TEST_SERVER_WALLET_ADDRESS_1;
import static tech.mogami.commons.test.BaseTestData.TEST_SERVER_WALLET_ADDRESS_2;

@SpringBootTest
@DisplayName("Verify Service Tests")
public class VerifyServiceTest {

    @Autowired
    private VerifyService verifyService;

    @Test
    @DisplayName("Empty request")
    public void testEmptyRequest() {
        assertThat(verifyService.verify(null))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.invalidReason()).isEqualTo("undefined");
                    assertThat(result.payer()).isNull();
                });
    }

    @Test
    @DisplayName("Invalid schemes")
    public void testInvalidSchemes() {
        assertThat(verifyService.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder().scheme("INVALID_SCHEME").build())
                        .paymentRequirements(PaymentRequirements.builder().scheme(EXACT_SCHEME.name()).build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.invalidReason()).isEqualTo("unsupported_scheme");
                    assertThat(result.payer()).isNull();    // TODO Add payer test
                });

        assertThat(verifyService.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder().scheme(EXACT_SCHEME.name()).build())
                        .paymentRequirements(PaymentRequirements.builder().scheme("INVALID_SCHEME").build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.invalidReason()).isEqualTo("unsupported_scheme");
                    assertThat(result.payer()).isNull();    // TODO Add payer test
                });
    }

    @Test
    @DisplayName("Invalid payment context")
    public void testInvalidPaymentContext() {
        assertThat(verifyService.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder().scheme(EXACT_SCHEME.name()).build())
                        .paymentRequirements(PaymentRequirements.builder().scheme(EXACT_SCHEME.name()).build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.invalidReason()).isEqualTo("invalid_network");
                    assertThat(result.payer()).isNull();    // TODO Add payer test
                });
    }

    @Test
    @DisplayName("Invalid signature")
    public void testInvalidSignature() {
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

        assertThat(verifyService.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(paymentPayload)
                        .paymentRequirements(paymentRequirements)
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.invalidReason()).isEqualTo("invalid_exact_evm_payload_signature");
                    assertThat(result.payer()).isNull();    // TODO Add payer test
                });
    }

    @Test
    @DisplayName("Payment address mismatch")
    public void testPaymentAddressMismatch() {
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
        PaymentRequirements paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.name())
                .maxAmountRequired("10000")
                .resource("http://localhost/weather")
                .payTo(TEST_SERVER_WALLET_ADDRESS_2)
                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                .build();

        assertThat(verifyService.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(paymentPayload)
                        .paymentRequirements(paymentRequirements)
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.invalidReason()).isEqualTo("invalid_exact_evm_payload_recipient_mismatch");
                    assertThat(result.payer()).isNull();    // TODO Add payer test
                });
    }

    @Test
    @DisplayName("Invalid validBefore")
    public void testInvalidValidBefore() {
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
                                .validBefore("1748534767") // This is valid
                                .nonce("0x9b750f5097972d82c02ac371278b83ecf3ca3be8387db59e664eb38c98f97a3d")
                                .build()
                        ).build()
                ).build();
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

        assertThat(verifyService.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(paymentPayload)
                        .paymentRequirements(paymentRequirements)
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.invalidReason()).isEqualTo("invalid_exact_evm_payload_authorization_valid_before");
                    assertThat(result.payer()).isNull();    // TODO Add payer test
                });
    }

}
