package tech.mogami.facilitator.test.verifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.PaymentPayload;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.facilitator.verifier.general.SchemeVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.mogami.commons.api.facilitator.VerificationError.UNSUPPORTED_SCHEME;
import static tech.mogami.commons.header.payment.schemes.Schemes.EXACT_SCHEME;

@SpringBootTest
@DisplayName("Scheme verifier tests")
public class SchemeVerifierTest {

    @Autowired
    private SchemeVerifier schemeVerifier;

    @Test
    @DisplayName("Schemes are not set")
    public void testSchemesNotSet() {
        assertThat(schemeVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder().build())
                        .paymentRequirements(PaymentRequirements.builder().build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(UNSUPPORTED_SCHEME);
                    assertThat(result.errorMessage()).isEqualTo("Scheme in payment payload is required");
                });

        assertThat(schemeVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder().scheme(EXACT_SCHEME.name()).build())
                        .paymentRequirements(PaymentRequirements.builder().build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(UNSUPPORTED_SCHEME);
                    assertThat(result.errorMessage()).isEqualTo("Scheme in payment requirements is required");
                });
    }

    @Test
    @DisplayName("Invalid schemes")
    public void testInvalidSchemes() {
        assertThat(schemeVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder().scheme("INVALID_PAYLOAD_SCHEME").build())
                        .paymentRequirements(PaymentRequirements.builder().build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(UNSUPPORTED_SCHEME);
                    assertThat(result.errorMessage()).isEqualTo("Scheme in payment payload is invalid (Your value: INVALID_PAYLOAD_SCHEME)");
                });

        assertThat(schemeVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder().scheme(EXACT_SCHEME.name()).build())
                        .paymentRequirements(PaymentRequirements.builder().scheme("INVALID_PAYMENT_SCHEME").build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(UNSUPPORTED_SCHEME);
                    assertThat(result.errorMessage()).isEqualTo("Scheme in payment requirements is invalid (Your value: INVALID_PAYMENT_SCHEME)");
                });
    }

    @Test
    @DisplayName("Valid schemes")
    public void testValidSchemes() {
        assertThat(schemeVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder().scheme(EXACT_SCHEME.name()).build())
                        .paymentRequirements(PaymentRequirements.builder().scheme(EXACT_SCHEME.name()).build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isTrue();
                    assertThat(result.verificationError()).isNull();
                    assertThat(result.errorMessage()).isNull();
                });
    }

}
