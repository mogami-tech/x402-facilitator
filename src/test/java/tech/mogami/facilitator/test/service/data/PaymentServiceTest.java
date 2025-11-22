package tech.mogami.facilitator.test.service.data;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;
import tech.mogami.facilitator.repository.PaymentRepository;
import tech.mogami.facilitator.service.data.PaymentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
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
        paymentService.logPaymentStep(PaymentStepDto.builder().paymentStepType(VERIFY).nonce("NONCE_00001").build());
        paymentService.logPaymentStep(PaymentStepDto.builder().paymentStepType(VERIFY).nonce("NONCE_00002").build());
        paymentService.logPaymentStep(PaymentStepDto.builder().paymentStepType(VERIFY).nonce("NONCE_00001").build());
        assertThat(paymentRepository.count()).isEqualTo(countBeforeCallingServices + 2);

        // Testing what we have on NONCE_00001 =========================================================================
        assertThat(paymentService.searchPaymentById("NONCE_00001")).isPresent().get()
                .satisfies(payment -> {
                    assertThat(payment.paymentId()).isEqualTo("NONCE_00001");
                    assertThat(payment.steps()).hasSize(2);
                });
    }

}
