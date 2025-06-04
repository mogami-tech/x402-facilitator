package tech.mogami.facilitator.test.verifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.header.payment.PaymentPayload;
import tech.mogami.commons.header.payment.PaymentRequirements;
import tech.mogami.facilitator.verifier.general.RequestNotEmptyVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.mogami.facilitator.verifier.VerificationError.UNDEFINED;

@SpringBootTest
@DisplayName("Request not empty Verifier Tests")
public class RequestNotEmptyVerifierTest {

    @Autowired
    private RequestNotEmptyVerifier requestNotEmptyVerifier;

    @Test
    @DisplayName("Request is empty")
    public void testRequestIsEmpty() {
        assertThat(requestNotEmptyVerifier.verify(null))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(UNDEFINED);
                    assertThat(result.errorMessage()).isEqualTo("The request object received is null");
                });
    }

    @Test
    @DisplayName("Payment payload is null")
    public void testPaymentPayloadIsNull() {
        assertThat(requestNotEmptyVerifier.verify(
                VerifyRequest.builder()
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(UNDEFINED);
                    assertThat(result.errorMessage()).isEqualTo("The payment payload in the request is null");
                });
    }

    @Test
    @DisplayName("Payment requirements is null")
    public void testPaymentRequirementsIsNull() {
        assertThat(requestNotEmptyVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder().build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(UNDEFINED);
                    assertThat(result.errorMessage()).isEqualTo("The payment requirements in the request is null");
                });
    }

    @Test
    @DisplayName("Request not empty - valid request")
    public void testRequestNotEmpty() {
        assertThat(requestNotEmptyVerifier.verify(
                VerifyRequest.builder()
                        .paymentPayload(PaymentPayload.builder().build())
                        .paymentRequirements(PaymentRequirements.builder().build())
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isTrue();
                    assertThat(result.verificationError()).isNull();
                    assertThat(result.errorMessage()).isNull();
                });
    }

}
