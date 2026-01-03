package tech.mogami.facilitator.test.core.verifier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.commons.api.facilitator.verify.VerificationRequest;
import tech.mogami.commons.payment.PaymentPayload;
import tech.mogami.commons.payment.PaymentRequirements;
import tech.mogami.commons.payment.PaymentResource;
import tech.mogami.facilitator.verifier.general.PaymentRequirementsVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.mogami.commons.constant.X402Error.INVALID_PAYMENT_REQUIREMENTS;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.version.X402Versions.X402_SUPPORTED_VERSION_BY_MOGAMI;
import static tech.mogami.commons.payment.schemes.Schemes.EXACT_SCHEME;
import static tech.mogami.commons.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_NAME;
import static tech.mogami.commons.payment.schemes.exact.ExactSchemeConstants.EXACT_SCHEME_PARAMETER_VERSION;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_2;

@SpringBootTest
@DisplayName("Payment requirements verifier tests")
public class PaymentRequirementsVerifierTest {

    @Autowired
    private PaymentRequirementsVerifier paymentRequirementsVerifier;

    @Test
    @DisplayName("Payment requirements verifier")
    public void paymentRequirementsVerifier() {
        var paymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.networkId())
                .amount("10000")
                .payTo(TEST_CLIENT_WALLET_ADDRESS_2)
                .maxTimeoutSeconds(60)
                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                .build();

        var acceptedPaymentRequirements = PaymentRequirements.builder()
                .scheme(EXACT_SCHEME.name())
                .network(BASE_SEPOLIA.networkId())
                .amount("20000")
                .payTo(TEST_CLIENT_WALLET_ADDRESS_2)
                .maxTimeoutSeconds(60)
                .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                .extra(EXACT_SCHEME_PARAMETER_NAME, "USDC")
                .extra(EXACT_SCHEME_PARAMETER_VERSION, "2")
                .build();

        // We give a different PaymentRequirements to accepted and payload.
        PaymentPayload paymentPayload = PaymentPayload.builder()
                .x402Version(X402_SUPPORTED_VERSION_BY_MOGAMI.version())
                .resource(PaymentResource.builder()
                        .url("https://example.com/resource/12345")
                        .build())
                .accepted(acceptedPaymentRequirements)
                .payload(paymentRequirements)
                .build();

        assertThat(paymentRequirementsVerifier.verify(
                VerificationRequest.builder()
                        .paymentRequirements(paymentRequirements)
                        .paymentPayload(paymentPayload)
                        .build()))
                .isNotNull()
                .satisfies(result -> {
                    assertThat(result.isValid()).isFalse();
                    assertThat(result.verificationError()).isEqualTo(INVALID_PAYMENT_REQUIREMENTS);
                    assertThat(result.errorMessage()).isEqualTo("Payment requirements do not match the accepted payment requirements");
                });
    }

}
