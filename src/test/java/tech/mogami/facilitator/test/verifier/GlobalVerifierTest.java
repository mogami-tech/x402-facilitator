package tech.mogami.facilitator.test.verifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.PaymentPayload;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.commons.util.NonceUtil;
import tech.mogami.facilitator.verifier.general.GlobalVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.mogami.commons.constant.X402Error.INVALID_PAYLOAD;
import static tech.mogami.commons.constant.X402Error.UNKNOWN;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;
import static tech.mogami.commons.header.payment.schemes.Schemes.EXACT_SCHEME;
import static tech.mogami.commons.header.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_NAME;
import static tech.mogami.commons.header.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_VERSION;
import static tech.mogami.commons.test.BaseTestData.TEST_CLIENT_WALLET_ADDRESS_1;
import static tech.mogami.commons.test.BaseTestData.TEST_SERVER_WALLET_ADDRESS_1;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Global verifier tests")
public class GlobalVerifierTest {

    @Autowired
    private GlobalVerifier globalVerifier;

    @Test
    @Order(1)
    @DisplayName("Request is empty")
    public void requestIsEmpty() {
        assertThat(globalVerifier.verify(null))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(UNKNOWN);
                    assertThat(result.errorMessage()).isEqualTo("The request object received is null");
                });
    }

    @Test
    @Order(2)
    @DisplayName("X402 version is null")
    public void x402VersionIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("x402 version in verify request is required");
                });
    }

    @Test
    @Order(3)
    @DisplayName("x402 version is invalid")
    public void x402VersionIsInvalid() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(0)
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("x402 version in verify request is invalid (Your value: 0)");
                });
    }

    @Test
    @Order(4)
    @DisplayName("Payment payload is null")
    public void paymentPayloadIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Payment payload in verify request is required");
                });
    }

    @Test
    @Order(5)
    @DisplayName("Payment payload - X402 version is null")
    public void paymentPayloadX402VersionIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder().build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("x402 version in payment payload is required");
                });
    }

    @Test
    @Order(6)
    @DisplayName("Payment payload - X402 version is invalid")
    public void paymentPayloadX402VersionIsInvalid() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(0)
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("x402 version in payment payload is invalid (Your value: 0)");
                });
    }

    @Test
    @Order(7)
    @DisplayName("Payment payload - Scheme is null")
    public void paymentPayloadSchemeIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Scheme in payment payload is required");
                });
    }

    @Test
    @Order(8)
    @DisplayName("Payment payload - Scheme is invalid")
    public void paymentPayloadSchemeIsInvalid() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme("invalid")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Scheme in payment payload is invalid (Your value: invalid)");
                });
    }

    @Test
    @Order(9)
    @DisplayName("Payment payload - Network is null")
    public void paymentPayloadNetworkIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Network in payment payload is required");
                });
    }

    @Test
    @Order(10)
    @DisplayName("Payment payload - Network is invalid")
    public void paymentPayloadNetworkIsInvalid() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network("invalid")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Network in payment payload is invalid (Your value: invalid)");
                });
    }

    @Test
    @Order(11)
    @DisplayName("Payment payload - Payload is null")
    public void paymentPayloadPayloadIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Payload in payment payload is required");
                });
    }

    @Test
    @Order(12)
    @DisplayName("Payment payload - Payload signature is null")
    public void paymentPayloadPayloadSignatureIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder().build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Signature in exact scheme payload is required");
                });
    }

    @Order(13)
    @Test
    @DisplayName("Payment payload - Payload authorization is null")
    public void paymentPayloadPayloadAuthorizationIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Authorization in exact scheme payload is required");
                });
    }

    @Test
    @Order(14)
    @DisplayName("Payment payload - Payload authorization from is null")
    public void paymentPayloadPayloadAuthorizationFromIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder().build())
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("From in exact scheme payload authorization is required");
                });
    }

    @Test
    @Order(15)
    @DisplayName("Payment payload - Payload authorization from address is invalid")
    public void paymentPayloadPayloadAuthorizationFromIsInvalid() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("invalid")
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("From in exact scheme payload authorization is invalid (Your value: invalid)");
                });
    }

    @Test
    @Order(16)
    @DisplayName("Payment payload - Payload authorization to is null")
    public void paymentPayloadPayloadAuthorizationToIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("To in exact scheme payload authorization is required");
                });
    }

    @Test
    @Order(17)
    @DisplayName("Payment payload - Payload authorization to address is invalid")
    public void paymentPayloadPayloadAuthorizationToIsInvalid() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("invalid")
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("To in exact scheme payload authorization is invalid (Your value: invalid)");
                });
    }

    @Test
    @Order(18)
    @DisplayName("Payment payload - Payload authorization value is null")
    public void paymentPayloadPayloadAuthorizationValueIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Amount value in exact scheme payload authorization is required");
                });
    }

    @Test
    @Order(19)
    @DisplayName("Payment payload - Payload authorization value is invalid")
    public void paymentPayloadPayloadAuthorizationValueIsInvalid() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("invalid")
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Amount value in exact scheme payload authorization is invalid (Your value: invalid)");
                });
    }

    @Test
    @Order(20)
    @DisplayName("Payment payload - Payload authorization validAfter is null")
    public void paymentPayloadPayloadAuthorizationValidAfterIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Valid after in exact scheme payload authorization is required");
                });
    }

    @Test
    @Order(21)
    @DisplayName("Payment payload - Payload authorization validBefore is invalid")
    public void paymentPayloadPayloadAuthorizationValidBeforeIsInvalid() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Valid before in exact scheme payload authorization is required");
                });
    }

    @Test
    @Order(22)
    @DisplayName("Payment payload - Payload authorization nonce is null")
    public void paymentPayloadPayloadAuthorizationNonceIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Nonce in exact scheme payload authorization is required");
                });
    }

    @Test
    @Order(23)
    @DisplayName("Payment requirements is null")
    public void paymentRequirementsIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .nonce("1")
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Payment requirements in verify request are required");
                });
    }

    @Test
    @Order(24)
    @DisplayName("Payment requirements - Scheme is null")
    public void paymentRequirementsSchemeIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .nonce("1")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder().build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Scheme in payment requirements is required");
                });
    }

    @Test
    @Order(25)
    @DisplayName("Payment requirements - Scheme is invalid")
    public void paymentRequirementsSchemeIsInvalid() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .nonce("1")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme("invalid")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Scheme in payment requirements is invalid (Your value: invalid)");
                });
    }

    @Test
    @Order(26)
    @DisplayName("Payment requirements - Network is null")
    public void paymentRequirementsNetworkIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .nonce("1")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Network in payment requirements is required");
                });
    }

    @Test
    @Order(27)
    @DisplayName("Payment requirements - Network is invalid")
    public void paymentRequirementsNetworkIsInvalid() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .nonce("1")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network("invalid")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Network in payment requirements is invalid (Your value: invalid)");
                });
    }

    @Test
    @Order(28)
    @DisplayName("Payment requirements - Max amount required is null")
    public void paymentRequirementsMaxAmountRequiredIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .nonce("1")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Maximum amount required in payment requirements is required");
                });
    }

    @Test
    @Order(29)
    @DisplayName("Payment requirements - Max amount required is invalid")
    public void paymentRequirementsMaxAmountRequiredIsInvalid() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .nonce("1")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .maxAmountRequired("invalid")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Maximum amount required in payment requirements is invalid (Your value: invalid)");
                });
    }

    @Test
    @Order(30)
    @DisplayName("Payment requirements - Resource is null")
    public void paymentRequirementsResourceIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .nonce("1")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .maxAmountRequired("1000000000000000000")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Resource in payment requirements is required");
                });
    }

    @Test
    @Order(31)
    @DisplayName("Payment requirements - payTo is null")
    public void paymentRequirementsPayToIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .nonce("1")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .maxAmountRequired("1000000000000000000")
                                .resource("https://example.com/resource")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Pay-to field in payment requirements is required");
                });
    }

    @Test
    @Order(32)
    @DisplayName("Payment requirements - payTo address is invalid")
    public void paymentRequirementsPayToIsInvalid() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .nonce("1")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .maxAmountRequired("1000000000000000000")
                                .resource("https://example.com/resource")
                                .payTo("invalid")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Pay-to field in payment requirements is invalid (Your value: invalid)");
                });
    }

    @Test
    @Order(33)
    @DisplayName("Payment requirements - maxTimeoutSeconds is null")
    public void paymentRequirementsMaxTimeoutSecondsIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .nonce("1")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .maxAmountRequired("1000000000000000000")
                                .resource("https://example.com/resource")
                                .payTo("0x1234567890abcdef1234567890abcdef12345678")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Maximum timeout seconds in payment requirements is required");
                });
    }

    @Test
    @Order(34)
    @DisplayName("Payment requirements - maxTimeoutSeconds is invalid")
    public void paymentRequirementsMaxTimeoutSecondsIsInvalid() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .nonce("1")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .maxAmountRequired("1000000000000000000")
                                .resource("https://example.com/resource")
                                .payTo("0x1234567890abcdef1234567890abcdef12345678")
                                .maxTimeoutSeconds(-1)
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Maximum timeout seconds in payment requirements must be a positive integer (Your value: -1)");
                });
    }

    @Test
    @Order(35)
    @DisplayName("Payment requirements - Asset is null")
    public void paymentRequirementsAssetIsNull() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .nonce("1")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .maxAmountRequired("1000000000000000000")
                                .resource("https://example.com/resource")
                                .payTo("0x1234567890abcdef1234567890abcdef12345678")
                                .maxTimeoutSeconds(60)
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Asset in payment requirements is required");
                });
    }

    @Test
    @Order(36)
    @DisplayName("Payment requirements - Asset is invalid")
    public void paymentRequirementsAssetIsInvalid() {
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                .to("0x1234567890abcdef1234567890abcdef12345678")
                                                .value("1000000000000000000")
                                                .validAfter("1718542400")
                                                .validBefore("1718642400")
                                                .nonce("1")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .maxAmountRequired("1000000000000000000")
                                .resource("https://example.com/resource")
                                .payTo("0x1234567890abcdef1234567890abcdef12345678")
                                .maxTimeoutSeconds(60)
                                .asset("invalid")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Asset in payment requirements is invalid (Your value: invalid)");
                });
    }

    @Test
    @Order(37)
    @DisplayName("No validation errors - all fields are valid")
    public void noValidationErrors() {
        long now = System.currentTimeMillis() / 1000;
        assertThat(globalVerifier.verify(
                VerifyRequest.builder()
                        .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("20000")
                                                .validAfter(String.valueOf(now))
                                                .validBefore(String.valueOf(now + 10))
                                                .nonce(NonceUtil.generateNonce())
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .maxAmountRequired("20000")
                                .resource("http://localhost/weather")
                                .payTo(TEST_SERVER_WALLET_ADDRESS_1)
                                .maxTimeoutSeconds(60)
                                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isTrue();
                    assertThat(result.verificationError()).isNull();
                    assertThat(result.errorMessage()).isNull();
                });
    }

}
