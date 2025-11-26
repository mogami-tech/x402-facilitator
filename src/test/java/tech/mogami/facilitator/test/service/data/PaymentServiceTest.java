package tech.mogami.facilitator.test.service.data;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.commons.api.facilitator.verify.VerifyRequest;
import tech.mogami.commons.api.facilitator.verify.VerifyResponse;
import tech.mogami.commons.constant.version.X402Versions;
import tech.mogami.commons.payment.PaymentPayload;
import tech.mogami.commons.payment.PaymentRequirements;
import tech.mogami.commons.payment.schemes.exact.ExactSchemePayload;
import tech.mogami.commons.util.JsonUtil;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;
import tech.mogami.facilitator.repository.PaymentRepository;
import tech.mogami.facilitator.service.data.PaymentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static tech.mogami.commons.constant.X402Error.INVALID_EXACT_EVM_PAYLOAD_SIGNATURE;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.payment.schemes.Schemes.EXACT_SCHEME;
import static tech.mogami.facilitator.domain.payment.PaymentStepType.VERIFY;

@SpringBootTest
@DisplayName("Payment service tests")
public class PaymentServiceTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    @Test
    @DisplayName("Test payment step log creation")
    public void testCreatePaymentStepLog() {
        // Invalid payment step should be refused ======================================================================
        assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> paymentService.logPaymentStep(null));
        assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> paymentService.logPaymentStep(PaymentStepDto.builder().build()));
        assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> paymentService.logPaymentStep(PaymentStepDto.builder().paymentStepType(VERIFY).build()));
        assertThatExceptionOfType(ConstraintViolationException.class)
                .isThrownBy(() -> paymentService.logPaymentStep(PaymentStepDto.builder().nonce("RANDOM_NONCE").build()));

        // Calling the service several times with the same nonce =======================================================
        final long countBeforeCallingServices = paymentRepository.count();
        paymentService.logPaymentStep(PaymentStepDto.builder()
                .paymentStepType(VERIFY)
                .nonce("NONCE_00001")
                .errorMessage("Invalid Json")
                .build());
        paymentService.logPaymentStep(PaymentStepDto.builder().paymentStepType(VERIFY).nonce("NONCE_00002").build());
        paymentService.logPaymentStep(PaymentStepDto.builder()
                .paymentStepType(VERIFY)
                .nonce("NONCE_00001")
                .errorCode("invalid_json")
                .build());
        assertThat(paymentRepository.count()).isEqualTo(countBeforeCallingServices + 2);

        // Testing what we have created ================================================================================
        assertThat(paymentService.searchPaymentById("NONCE_00001")).isPresent().get()
                .satisfies(payment -> {
                    assertThat(payment.paymentId()).isEqualTo("NONCE_00001");
                    assertThat(payment.steps()).hasSize(2);
                });
        assertThat(paymentService.searchPaymentById("NONCE_00002")).isPresent().get()
                .satisfies(payment -> {
                    assertThat(payment.paymentId()).isEqualTo("NONCE_00002");
                    assertThat(payment.steps()).hasSize(1);
                });

        // We add a failed verify step without signature ===============================================================
        paymentService.logPaymentStep(PaymentStepDto.builder()
                .paymentStepType(VERIFY)
                .nonce("NONCE_00001")
                .requestPayload(JsonUtil.toPrettyJson(
                        VerifyRequest.builder()
                                .x402Version(X402Versions.V1.version())
                                .paymentPayload(PaymentPayload.builder()
                                        .x402Version(X402Versions.V1.version())
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.name())
                                        .payload(ExactSchemePayload.builder()
                                                .signature("")
                                                .authorization(
                                                        ExactSchemePayload.Authorization.builder()
                                                                .from("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                                .to("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                                                .value("1000")
                                                                .validAfter("1747601321")
                                                                .validBefore("1747601441")
                                                                .nonce("0xa5f2264bcb079c96f07d9fe1378f5f846e3acd5ea29024d740cec881e2e85fe6")
                                                                .build()
                                                )
                                                .build()
                                        )
                                        .build()
                                )
                                .paymentRequirements(PaymentRequirements.builder()
                                        .scheme(EXACT_SCHEME.name())
                                        .network(BASE_SEPOLIA.name())
                                        .maxAmountRequired("1000")
                                        .resource("http://localhost:4021/weather")
                                        .description("")
                                        .mimeType("")
                                        .payTo("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                        .maxTimeoutSeconds(60)
                                        .asset("0x036CbD53842c5426634e7929541eC2318f3dCF7e")
                                        .extra("name", "USDC")
                                        .extra("version", "2")
                                        .build()
                                ).build()))
                .responsePayload(JsonUtil.toPrettyJson(
                        VerifyResponse.builder()
                                .isValid(false)
                                .invalidReason(INVALID_EXACT_EVM_PAYLOAD_SIGNATURE.getCode())
                                .payer("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73")
                                .build()
                ))
                .errorCode(INVALID_EXACT_EVM_PAYLOAD_SIGNATURE.getCode())
                .errorMessage(INVALID_EXACT_EVM_PAYLOAD_SIGNATURE.getDefaultMessage())
                .build());
        assertThat(paymentRepository.count()).isEqualTo(countBeforeCallingServices + 2);
        // TODO only take into account the steps where we have a proper request/response payload ?

        // We now check the value we can get from the payment step =====================================================
        assertThat(paymentService.searchPaymentById("NONCE_00001")).isPresent().get()
                .satisfies(payment -> {
                    assertThat(payment.paymentId()).isEqualTo("NONCE_00001");
                    assertThat(payment.steps()).hasSize(3);
                    assertThat(payment.paymentId()).isNotNull();
                    assertThat(payment.fromAddress().address()).isEqualTo("0x2980bc24bBFB34DE1BBC91479Cb712ffbCE02F73");
                });


    }

}
