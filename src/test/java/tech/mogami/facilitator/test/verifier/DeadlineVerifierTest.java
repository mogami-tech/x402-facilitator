package tech.mogami.facilitator.test.verifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.PaymentPayload;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.commons.header.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.facilitator.verifier.exact.DeadlineVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.mogami.commons.api.facilitator.VerificationError.INVALID_EXACT_EVM_PAYLOAD_VALID_AFTER;
import static tech.mogami.commons.api.facilitator.VerificationError.INVALID_EXACT_EVM_PAYLOAD_VALID_BEFORE;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.header.payment.schemes.Schemes.EXACT_SCHEME;

@SpringBootTest
@DisplayName("Deadline verifier tests")
public class DeadlineVerifierTest {

    @Autowired
    private DeadlineVerifier deadlineVerifier;

    @Test
    @DisplayName("validBefore test")
    public void testValidBefore() {
        assertThat(deadlineVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .authorization(
                                                ExactSchemePayload.Authorization.builder()
                                                        .validBefore(String.valueOf((System.currentTimeMillis() / 1000) + 5))
                                                        .validAfter(String.valueOf((System.currentTimeMillis() / 100) + 60))
                                                        .build()
                                        )
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
                    assertThat(result.verificationError()).isEqualTo(INVALID_EXACT_EVM_PAYLOAD_VALID_BEFORE);
                    assertThat(result.errorMessage()).isEqualTo("Authorization 'validBefore' is in the past or too close to the current time");
                });
    }

    @Test
    @DisplayName("validAfter test")
    public void testValidAfter() {
        assertThat(deadlineVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder()
                                .scheme(EXACT_SCHEME.name())
                                .network(BASE_SEPOLIA.name())
                                .payload(ExactSchemePayload.builder()
                                        .authorization(
                                                ExactSchemePayload.Authorization.builder()
                                                        .validBefore(String.valueOf(((System.currentTimeMillis() / 1000) + 60)))
                                                        .validAfter(String.valueOf((System.currentTimeMillis() / 100) + 60))
                                                        .build()
                                        )
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
                    assertThat(result.verificationError()).isEqualTo(INVALID_EXACT_EVM_PAYLOAD_VALID_AFTER);
                    assertThat(result.errorMessage()).isEqualTo("Authorization 'validAfter' is in the future");
                });
    }

}
