package tech.mogami.facilitator.test.verifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.PaymentPayload;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.verifier.exact.PaymentValueVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.mogami.facilitator.verifier.VerificationError.INSUFFICIENT_PAYMENT_VALUE;

@SpringBootTest
@DisplayName("Payment value verifier tests")
public class PaymentValueVerifierTest {

    @Autowired
    PaymentValueVerifier paymentValueVerifier;

    @Test
    @DisplayName("Payload value is not enough")
    public void payloadValueIsNotEnoughTest() {
        assertThat(paymentValueVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .payload(ExactSchemePayload.builder()
                                        .authorization(
                                                ExactSchemePayload.Authorization.builder()
                                                        .value("100") // Value is less than required
                                                        .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .maxAmountRequired("110")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INSUFFICIENT_PAYMENT_VALUE);
                    assertThat(result.errorMessage()).isEqualTo("Payment value is less than the required maximum amount (100 < 110)");
                });
    }

    @Test
    @DisplayName("Payload value is enough")
    public void payloadValueIsEnoughTest() {
        assertThat(paymentValueVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .payload(ExactSchemePayload.builder()
                                        .authorization(
                                                ExactSchemePayload.Authorization.builder()
                                                        .value("110") // Value is enough
                                                        .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .maxAmountRequired("110")
                                .build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isTrue();
                    assertThat(result.verificationError()).isNull();
                    assertThat(result.errorMessage()).isNull();
                });
    }

    @Test
    @DisplayName("Payload value is superior to required amount")
    public void payloadValueIsSuperiorTest() {
        assertThat(paymentValueVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .payload(ExactSchemePayload.builder()
                                        .authorization(
                                                ExactSchemePayload.Authorization.builder()
                                                        .value("120") // Value is superior to required
                                                        .build())
                                        .build())
                                .build())
                        .paymentRequirements(PaymentRequirements.builder()
                                .maxAmountRequired("110")
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
