package tech.mogami.facilitator.test.service.data;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tech.mogami.facilitator.domain.payment.PaymentStepType;
import tech.mogami.facilitator.dto.payment.PaymentStepDto;
import tech.mogami.facilitator.repository.PaymentRepository;
import tech.mogami.facilitator.service.data.PaymentService;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"scheduler-disabled"})
@DisplayName("Payment service concurrency tests")
public class PaymentServiceConcurrencyTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @Transactional
    @DisplayName("Test concurrent payment processing")
    public void testConcurrentPaymentProcessing() throws InterruptedException {
        // Configuration.
        final String nonce = "nonce-" + UUID.randomUUID();
        final int THREADS = 10;
        final int STEPS_PER_THREAD = 5;
        final int expectedSteps = THREADS * STEPS_PER_THREAD;

        // Executor setup.
        try (ExecutorService executor = Executors.newFixedThreadPool(THREADS)) {
            CountDownLatch ready = new CountDownLatch(THREADS);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch done = new CountDownLatch(THREADS);

            for (int t = 0; t < THREADS; t++) {
                executor.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                    } catch (InterruptedException ignored) {
                    }
                    try {
                        for (int s = 0; s < STEPS_PER_THREAD; s++) {
                            paymentService.logPaymentStep(PaymentStepDto.builder()
                                    .nonce(nonce)
                                    .paymentStepType(PaymentStepType.VERIFY)
                                    .requestPayload("{\"req\":" + s + "}")
                                    .responsePayload("{\"resp\":true}")
                                    .build());
                        }
                    } catch (Exception e) {
                        Assertions.fail("Exception in thread: " + e.getMessage());
                    } finally {
                        done.countDown();
                    }
                });
            }

            // Waiting.
            ready.await();
            start.countDown();
            done.await();
            executor.shutdown();
            Thread.sleep(Duration.ofMinutes(1).toMillis());

            // Test the result.e
            var payment = paymentRepository.findByPaymentId(nonce);
            assertThat(payment)
                    .isPresent()
                    .get()
                    .satisfies(p -> assertThat(p.getSteps()).hasSize(expectedSteps));
        }

    }

}
