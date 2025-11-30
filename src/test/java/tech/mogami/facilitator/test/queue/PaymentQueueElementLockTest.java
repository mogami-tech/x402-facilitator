package tech.mogami.facilitator.test.queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import tech.mogami.facilitator.domain.queue.PaymentQueueElement;
import tech.mogami.facilitator.repository.PaymentQueueElementRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:16:///explorer",
        "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
@AutoConfigureMockMvc
@DisplayName("Payment queue element lock tests")
public class PaymentQueueElementLockTest {

    /** Test payment ID */
    private static final String PAYMENT_ID = "TEST-123";

    @Autowired
    PaymentQueueElementRepository repository;

    @Autowired
    TransactionTemplate txTemplate;

    @BeforeEach
    void setup() {
        // We insert one element to be locked.
        repository.deleteAll();
        PaymentQueueElement e = PaymentQueueElement.builder()
                .paymentId(PAYMENT_ID)
                .build();
        repository.save(e);
    }

    @Test
    @DisplayName("Two threads trying to lock the same element, only one should succeed")
    void twoThreadsGettingTheSameLock() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // To contain the results ======================================================================================
        AtomicReference<Optional<PaymentQueueElement>> result1 = new AtomicReference<>();
        AtomicReference<Optional<PaymentQueueElement>> result2 = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        // Running two threads at the "same moment" ====================================================================
        Callable<Void> task1 = () -> {
            latch.await();
            result1.set(repository.lockOneElement());
            return null;
        };
        Callable<Void> task2 = () -> {
            latch.await();
            result2.set(repository.lockOneElement());
            return null;
        };
        Future<Void> f1 = executor.submit(task1);
        Future<Void> f2 = executor.submit(task2);
        latch.countDown();
        f1.get();
        f2.get();
        executor.shutdown();

        // Checking that only one thread acquired the lock =============================================================
        assertThat(result1.get().isEmpty() ^ result2.get().isEmpty())
                .as("One and only one thread must acquire the lock")
                .isTrue();

        assertThat(result1.get().or(result2::get))
                .isPresent().get()
                .extracting(PaymentQueueElement::getPaymentId).isEqualTo(PAYMENT_ID);
    }

    @Test
    @DisplayName("Insert should wait and succeed if the element is locked by another transaction")
    void insertShouldWaitAndSucceedIfLocked() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch batchHasLockedLatch = new CountDownLatch(1);

        // We enqueue an element with PAYMENT_ID
        repository.enqueuePayment(PAYMENT_ID, Instant.now());

        // First thread locks and processes the element ================================================================
        Future<?> batchFuture = executor.submit(() -> {
            txTemplate.executeWithoutResult(status -> {
                // A. We lock an element.
                Optional<PaymentQueueElement> element = repository.lockOneElement();
                assertThat(element).isPresent().get()
                        .extracting(PaymentQueueElement::getPaymentId)
                        .isEqualTo(PAYMENT_ID);

                // B. We signal that we have the lock now so the other thread can proceed.
                batchHasLockedLatch.countDown();

                // C. We simulate a long processing.
                try {
                    System.out.println("[Batch] I have a lock on the element. Processing...");
                    Thread.sleep(Duration.ofSeconds(30).toMillis());
                } catch (InterruptedException e) {
                }

                // D. We're done, we delete the element.
                repository.delete(element.get());
                System.out.println("[Batch] Processing done, element deleted");
            });
            // E. Commit done!
            System.out.println("[Batch] Transaction committed.");
        });

        // The Second thread tries to insert the same PAYMENT_ID while the first batch is processing ==================
        Future<?> producerFuture = executor.submit(() -> {
            try {
                // We are waiting for the first batch to have locked the element.
                batchHasLockedLatch.await();
                System.out.println("[Producer] I try to insert the same PAYMENT_ID while it's locked by another transaction");

                // We try to enqueue the same PAYMENT_ID
                // This should block until the first transaction is committed.
                repository.enqueuePayment(PAYMENT_ID, Instant.now());

                System.out.println("[Producer] Insertion was made. Postgres made me wait");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Waiting for both to finish ==================================================================================
        batchFuture.get();
        producerFuture.get();
        executor.shutdown();

        // Verifications ===============================================================================================

        // We should still have to process one element in the queue
        long count = repository.count();
        assertThat(count).isEqualTo(1);

        // The one to be treated should be the one with PAYMENT_ID
        assertThat(repository.findAll().getFirst().getPaymentId()).isEqualTo(PAYMENT_ID);
    }

}
