package tech.mogami.facilitator.test.core.outbox;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import tech.mogami.facilitator.outbox.NewPaymentStepMessage;
import tech.mogami.facilitator.provider.outbox.repository.OutboxEventRepository;
import tech.mogami.facilitator.provider.outbox.service.OutboxService;
import tech.mogami.facilitator.repository.PaymentRepository;
import tech.mogami.facilitator.test.core.util.BaseTest;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static tech.mogami.commons.constant.X402Error.INVALID_EXACT_EVM_PAYLOAD_SIGNATURE;
import static tech.mogami.commons.constant.network.Networks.BASE_SEPOLIA;
import static tech.mogami.commons.constant.network.contract.BaseContracts.BASE_SEPOLIA_USDC_CONTRACT;
import static tech.mogami.commons.constant.version.X402Versions.V1;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_1;
import static tech.mogami.commons.test.BaseMogamiTestData.TEST_CLIENT_WALLET_ADDRESS_2;
import static tech.mogami.facilitator.domain.payment.PaymentStepType.VERIFY;
import static tech.mogami.facilitator.provider.outbox.domain.OutboxEventStatus.DONE;

@SpringBootTest
@Transactional
@DisplayName("Outbox behavior tests")
public class OutboxTest extends BaseTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxService outboxService;

    @Test
    @DisplayName("Multithread outbox event processing")
    public void testMultithreadOutboxEventProcessing() throws InterruptedException {

        // Constants ===================================================================================================
        final int THREADS = 30;
        final int EVENTS_PER_THREAD = 4;
        final int TOTAL_EVENTS_EXPECTED = THREADS * EVENTS_PER_THREAD;

        // Setup ========================================================================================================
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch latch = new CountDownLatch(THREADS);

        // Run threads to publish outbox events ========================================================================
        for (int i = 0; i < THREADS; i++) {
            final String nonce = "outbox-event-nonce-" + i;
            pool.submit(() -> {
                try {
                    for (int j = 0; j < EVENTS_PER_THREAD; j++) {

                        outboxService.publish(
                                NewPaymentStepMessage.builder()
                                        .paymentId(nonce)
                                        .paymentStepType(VERIFY)
                                        .requestPayload(getVerificationRequest(
                                                V1,
                                                BASE_SEPOLIA,
                                                TEST_CLIENT_WALLET_ADDRESS_1,
                                                TEST_CLIENT_WALLET_ADDRESS_2,
                                                BASE_SEPOLIA_USDC_CONTRACT,
                                                "10000",
                                                nonce))
                                        .responsePayload(getVerifyResponse(
                                                false,
                                                INVALID_EXACT_EVM_PAYLOAD_SIGNATURE,
                                                TEST_CLIENT_WALLET_ADDRESS_1
                                        ))
                                        .build()
                        );
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to finish ==============================================================================
        latch.await();
        pool.shutdown();
        await().atMost(Duration.ofMinutes(2))
                .until(() -> outboxEventRepository.findAll()
                        .stream()
                        .allMatch(event -> event.getStatus().isFinal()));

        // Verify results ==============================================================================================
        // All events are treated as done
        var eventsDone = outboxEventRepository.findAll().stream().filter(event -> event.getStatus() == DONE).count();
        assertThat(eventsDone).isEqualTo(TOTAL_EVENTS_EXPECTED);

        // All payments and payment steps have been created
        assertThat(paymentRepository.findAll()
                .stream()
                .filter(payment -> StringUtils.startsWith(payment.getPaymentId(), "outbox-event-nonce-"))
                .count()).isEqualTo(THREADS);
        assertThat(paymentRepository.findAll()
                .stream()
                .filter(payment -> StringUtils.startsWith(payment.getPaymentId(), "outbox-event-nonce-"))
                .filter(payment -> payment.getSteps().size() != EVENTS_PER_THREAD)
                .findAny()).isEmpty();

    }

}
