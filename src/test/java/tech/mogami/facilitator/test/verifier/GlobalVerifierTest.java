package tech.mogami.facilitator.test.verifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
import tech.mogami.commons.payment.PaymentPayload;
import tech.mogami.commons.payment.PaymentRequirements;
import tech.mogami.commons.payment.PaymentResource;
import tech.mogami.commons.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.verifier.general.GlobalVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.mogami.commons.constant.X402Error.INVALID_PAYLOAD;
import static tech.mogami.commons.constant.X402Error.UNKNOWN;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;
import static tech.mogami.commons.payment.schemes.Schemes.EXACT_SCHEME;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_1;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_SERVER_WALLET_ADDRESS_1;

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
    @Order(100)
    @DisplayName("paymentPayload")
    public void paymentPayload() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Payment payload in verify request is required");
                });
    }

    @Test
    @Order(200)
    @DisplayName("x402paymentPayload.x402Version")
    public void paymentPayloadX402Version() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder().build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("x402 version in payment payload is required");
                });

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
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

        // TODO Add a verifier to check if the x402 version is supported
//        assertThat(globalVerifier.verify(
//                VerificationRequest.builder()
//                        .paymentPayload(PaymentPayload.builder()
//                                .x402Version(1)
//                                .build())
//                        .build()))
//                .isNotNull()
//                .satisfies(result -> {
//                    assertThat(result.isValid()).isFalse();
//                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
//                    assertThat(result.errorMessage()).isEqualTo("x402 version in payment payload is invalid (Your value: 0)");
//                });
    }

    @Test
    @Order(300)
    @DisplayName("paymentPayload.resource")
    public void paymentPayloadResource() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Resource in payment payload is required");
                });
    }

    @Test
    @Order(301)
    @DisplayName("paymentPayload.resource.url")
    public void paymentPayloadResourceUrl() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder().build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("URL of the protected resource is required");
                });
    }

    @Test
    @Order(400)
    @DisplayName("paymentPayload.accepted")
    public void paymentPayloadAccepted() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Payment requirements in payment payload is required");
                });
    }

    @Test
    @Order(401)
    @DisplayName("paymentPayload.accepted.scheme")
    public void paymentPayloadAcceptedScheme() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder().build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Scheme in payment requirements is required");
                });

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme("invalid")
                                        .build())
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
    @Order(402)
    @DisplayName("paymentPayload.accepted.network")
    public void paymentPayloadAcceptedNetwork() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Network in payment requirements is required");
                });

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network("invalid")
                                        .build())
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
    @Order(403)
    @DisplayName("paymentPayload.accepted.amount")
    public void paymentPayloadAcceptedAmount() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Amount in payment requirements is required");
                });
    }

    @Test
    @Order(404)
    @DisplayName("paymentPayload.accepted.asset")
    public void paymentPayloadAcceptedAsset() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Asset in payment requirements is required");
                });

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset("invalid")
                                        .build())
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
    @Order(405)
    @DisplayName("paymentPayload.accepted.payTo")
    public void paymentPayloadAcceptedPayTo() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Pay-to in payment requirements is required");
                });

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo("invalid")
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Pay-to in payment requirements is invalid (Your value: invalid)");
                });
    }

    @Test
    @Order(406)
    @DisplayName("paymentPayload.accepted.maxTimeoutSeconds")
    public void paymentPayloadAcceptedMaxTimeoutSeconds() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Maximum timeout seconds in payment requirements is required");
                });

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(-2)
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Maximum timeout seconds in payment requirements must be a positive integer (Your value: -2)");
                });
    }

    @Test
    @Order(500)
    @DisplayName("paymentPayload.payload")
    public void paymentPayloadPayload() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
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
    @Order(501)
    @DisplayName("paymentPayload.payload.signature")
    public void paymentPayloadPayloadSignature() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder().build()) // Empty payload object
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Signature in exact scheme payload is required");
                });
    }

    @Test
    @Order(502)
    @DisplayName("paymentPayload.payload.authorization")
    public void paymentPayloadPayloadAuthorization() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
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
    @Order(503)
    @DisplayName("paymentPayload.payload.authorization.from")
    public void paymentPayloadPayloadAuthorizationFrom() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
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

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
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
    @Order(504)
    @DisplayName("paymentPayload.payload.authorization.to")
    public void paymentPayloadPayloadAuthorizationTo() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
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

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
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
    @Order(505)
    @DisplayName("paymentPayload.payload.authorization.value")
    public void paymentPayloadPayloadAuthorizationValue() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
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

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
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
    @Order(506)
    @DisplayName("paymentPayload.payload.authorization.validAfter")
    public void paymentPayloadPayloadAuthorizationValidAfter() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
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

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("invalid")
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Valid after in exact scheme payload authorization is invalid (Your value: invalid)");
                });
    }

    @Test
    @Order(507)
    @DisplayName("paymentPayload.payload.authorization.validBefore")
    public void paymentPayloadPayloadAuthorizationValidBefore() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
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

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("invalid")
                                                .build())
                                        .build())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Valid before in exact scheme payload authorization is invalid (Your value: invalid)");
                });
    }

    @Test
    @Order(508)
    @DisplayName("paymentPayload.payload.authorization.nonce")
    public void paymentPayloadPayloadAuthorizationNonce() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
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
    @Order(500)
    @DisplayName("paymentRequirements")
    public void paymentRequirements() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
                                                .nonce("unique-nonce-123")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(null) // Missing payment requirements
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Payment requirements in verify request are required");
                });
    }

    @Test
    @Order(601)
    @DisplayName("paymentRequirements.scheme")
    public void paymentRequirementsScheme() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
                                                .nonce("unique-nonce-123")
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

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
                                                .nonce("unique-nonce-123")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(
                                PaymentRequirements.builder()
                                        .scheme("INVALID_SCHEME") // Invalid scheme
                                        .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Scheme in payment requirements is invalid (Your value: INVALID_SCHEME)");
                });
    }

    @Test
    @Order(602)
    @DisplayName("paymentRequirements.network")
    public void paymentRequirementsNetwork() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
                                                .nonce("unique-nonce-123")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(
                                PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        // Missing network
                                        .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Network in payment requirements is required");
                });

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
                                                .nonce("unique-nonce-123")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(
                                PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network("INVALID_NETWORK") // Invalid network
                                        .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Network in payment requirements is invalid (Your value: INVALID_NETWORK)");
                });
    }

    @Test
    @Order(603)
    @DisplayName("paymentRequirements.amount")
    public void paymentRequirementsAmount() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
                                                .nonce("unique-nonce-123")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(
                                PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        // Missing amount
                                        .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Amount in payment requirements is required");
                });

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
                                                .nonce("unique-nonce-123")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(
                                PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("invalid") // Invalid amount
                                        .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Amount in payment requirements is invalid (Your value: invalid)");
                });
    }

    @Test
    @Order(604)
    @DisplayName("paymentRequirements.asset")
    public void paymentRequirementsAsset() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
                                                .nonce("unique-nonce-123")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(
                                PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        // Missing asset
                                        .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Asset in payment requirements is required");
                });

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
                                                .nonce("unique-nonce-123")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(
                                PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset("invalid-asset-address") // Invalid asset
                                        .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Asset in payment requirements is invalid (Your value: invalid-asset-address)");
                });
    }

    @Test
    @Order(605)
    @DisplayName("paymentRequirements.payTo")
    public void paymentRequirementsPayTo() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
                                                .nonce("unique-nonce-123")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(
                                PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        // Missing payTo
                                        .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Pay-to in payment requirements is required");
                });

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
                                                .nonce("unique-nonce-123")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(
                                PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo("invalid-payto-address") // Invalid payTo
                                        .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Pay-to in payment requirements is invalid (Your value: invalid-payto-address)");
                });
    }

    @Test
    @Order(606)
    @DisplayName("paymentRequirements.maxTimeoutSeconds")
    public void paymentRequirementsMaxTimeoutSeconds() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
                                                .nonce("unique-nonce-123")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(
                                PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        // maxTimeoutSeconds missing
                                        .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Maximum timeout seconds in payment requirements is required");
                });

        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
                                                .nonce("unique-nonce-123")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(
                                PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(-10) // Invalid maxTimeoutSeconds
                                        .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYLOAD);
                    assertThat(result.errorMessage()).isEqualTo("Maximum timeout seconds in payment requirements must be a positive integer (Your value: -10)");
                });
    }

    @Test
    @Order(999)
    @DisplayName("Valid request")
    public void validRequest() {
        assertThat(globalVerifier.verify(
                VerificationRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                                .resource(PaymentResource.builder()
                                        .url("https://example.com/protected/resource")
                                        .build())
                                .accepted(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(60)
                                        .build())
                                .payload(ExactSchemePayload.builder()
                                        .signature("0xABCDEF1234567890")
                                        .authorization(ExactSchemePayload.Authorization.builder()
                                                .from(TEST_CLIENT_WALLET_ADDRESS_1)
                                                .to(TEST_SERVER_WALLET_ADDRESS_1)
                                                .value("100000")
                                                .validAfter("1740672089")
                                                .validBefore("1740672154")
                                                .nonce("unique-nonce-123")
                                                .build())
                                        .build())
                                .build())
                        .paymentRequirements(
                                PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.networkId())
                                        .amount("100000")
                                        .asset(TEST_SERVER_WALLET_ADDRESS_1)
                                        .payTo(TEST_CLIENT_WALLET_ADDRESS_1)
                                        .maxTimeoutSeconds(10)
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
