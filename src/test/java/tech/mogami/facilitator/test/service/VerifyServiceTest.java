package tech.mogami.facilitator.test.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.PaymentPayload;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.facilitator.service.VerifyService;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.mogami.commons.header.payment.schemes.Schemes.EXACT_SCHEME;

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

}
