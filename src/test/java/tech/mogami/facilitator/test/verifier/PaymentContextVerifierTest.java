package tech.mogami.facilitator.test.verifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.PaymentPayload;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.verifier.exact.PaymentContextVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.header.payment.schemes.Schemes.EXACT_SCHEME;
import static tech.mogami.commons.header.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_NAME;
import static tech.mogami.commons.header.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_VERSION;
import static tech.mogami.facilitator.verifier.VerificationError.INVALID_NETWORK;

@SpringBootTest
@DisplayName("Payment context verifier tests")
public class PaymentContextVerifierTest {

    @Autowired
    private PaymentContextVerifier paymentContextVerifier;

    @Test
    @DisplayName("Invalid network")
    public void testInvalidNetwork() {
        // On payment payload.
        assertThat(paymentContextVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .scheme(EXACT_SCHEME.name())
                                .payload(ExactSchemePayload.builder().build()).build())
                        .paymentRequirements(PaymentRequirements
                                .builder()
                                .scheme(EXACT_SCHEME.name()).build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_NETWORK);
                    assertThat(result.errorMessage()).isEqualTo("Network in payment payload is required");
                });

        assertThat(paymentContextVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network("INVALID_NETWORK")
                                .payload(ExactSchemePayload.builder().build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder().scheme(EXACT_SCHEME.name()).build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_NETWORK);
                    assertThat(result.errorMessage()).isEqualTo("Network in payment payload is invalid (Your value: INVALID_NETWORK)");
                });

        // On payment requirements.
        assertThat(paymentContextVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder().build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder().scheme(EXACT_SCHEME.name()).build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_NETWORK);
                    assertThat(result.errorMessage()).isEqualTo("Network in payment requirements is required");
                });

        assertThat(paymentContextVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder().build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network("INVALID_NETWORK")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_NETWORK);
                    assertThat(result.errorMessage()).isEqualTo("Network in payment requirements is invalid (Your value: INVALID_NETWORK)");
                });
    }

    @Test
    @DisplayName("Invalid payload")
    public void testInvalidPayload() {
        assertThat(paymentContextVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload("Invalid Payload")
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_NETWORK);
                    assertThat(result.errorMessage()).isEqualTo("Payment payload is not valid (Not an ExactSchemePayload)");
                });
    }

    @Test
    @DisplayName("Valid stablecoin name")
    public void testValidStablecoinName() {
        assertThat(paymentContextVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder().build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_NETWORK);
                    assertThat(result.errorMessage()).isEqualTo("Stablecoin name is not provided in the payment requirements");
                });

        assertThat(paymentContextVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder().build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .extra(EXACT_SCHEME_PARAMETER_NAME, "INVALID_STABLECOIN_NAME")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_NETWORK);
                    assertThat(result.errorMessage()).isEqualTo("Stablecoin name is invalid: INVALID_STABLECOIN_NAME");
                });
    }

    @Test
    @DisplayName("Valid exact scheme version")
    public void testValidExactSchemeVersion() {
        assertThat(paymentContextVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder().build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_NETWORK);
                    assertThat(result.errorMessage()).isEqualTo("Exact scheme version is not provided in the payment requirements");
                });

        assertThat(paymentContextVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder().build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_NETWORK);
                    assertThat(result.errorMessage()).isEqualTo("Exact scheme version is not provided in the payment requirements");
                });
    }

    @Test
    @DisplayName("Valid asset contract address")
    public void testValidAssetContractAddress() {
        assertThat(paymentContextVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder().build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_NETWORK);
                    assertThat(result.errorMessage()).isEqualTo("Asset in payment requirements is required");
                });

        assertThat(paymentContextVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder().build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .asset("0x12345678902345678")
                                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_NETWORK);
                    assertThat(result.errorMessage()).isEqualTo("Asset in payment requirements is invalid (Your value: 0x12345678902345678)");
                });
    }

    @Test
    @DisplayName("Valid schemes")
    public void testValidSchemes() {
        assertThat(paymentContextVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder().build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isTrue();
                });
    }

}
