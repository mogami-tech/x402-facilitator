package tech.mogami.facilitator.test.service.facilitator;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;
import tech.mogami.facilitator.repository.PaymentRepository;
import tech.mogami.facilitator.service.data.PaymentLogService;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.mogami.facilitator.domain.payment.PaymentStepType.VERIFY;

@SpringBootTest
@DisplayName("Payment service log tests")
public class PaymentLogServiceTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentLogService paymentLogService;

    @Test
    @Disabled
    @DisplayName("Test payment log creation")
    public void testCreatePaymentLog() {
        final long countBeforeCallingServices = paymentRepository.count();

        // Calling the service several times ===========================================================================
        paymentLogService.logPaymentStep(PaymentStepDto.builder()
                .paymentStepType(VERIFY)
                .nonce("NONCE_00001")
                .build());
        paymentLogService.logPaymentStep(PaymentStepDto.builder()
                .paymentStepType(VERIFY)
                .nonce("NONCE_00002")
                .build());
        paymentLogService.logPaymentStep(PaymentStepDto.builder()
                .paymentStepType(VERIFY)
                .nonce("NONCE_00001")
                .build());

        // Checking that only 2 payments were created ==================================================================
        assertThat(paymentRepository.count()).isEqualTo(countBeforeCallingServices + 2);
        assertThat(paymentRepository.findByPaymentId("NONCE_00001"))
                .isPresent()
                .get()
                .satisfies(payment -> {
                    assertThat(payment.getPaymentId()).isEqualTo("NONCE_00001");


                });

    }

}
