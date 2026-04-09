package tech.mogami.facilitator.test.core.outbox;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tech.mogami.facilitator.provider.outbox.batch.OutboxPurgeBatch;
import tech.mogami.facilitator.provider.outbox.domain.OutboxEvent;
import tech.mogami.facilitator.provider.outbox.domain.OutboxEventStatus;
import tech.mogami.facilitator.provider.outbox.repository.OutboxEventRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.mogami.facilitator.provider.outbox.domain.OutboxEventStatus.DONE;
import static tech.mogami.facilitator.provider.outbox.domain.OutboxEventStatus.ERROR;
import static tech.mogami.facilitator.provider.outbox.domain.OutboxEventType.NEW_PAYMENT_STEP;

@SpringBootTest
@DisplayName("Outbox purge batch tests")
public class OutboxPurgeBatchTest {

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @Autowired
    OutboxPurgeBatch outboxPurgeBatch;

    @BeforeEach
    void cleanup() {
        outboxEventRepository.deleteAll();
    }

    @Test
    @DisplayName("Purge batch test")
    public void purgeBatchTest() {
        Instant now = Instant.now();

        // Done events =================================================================================================
        long event01 = createOutboxEvent(DONE, now.minus(Duration.ofDays(8)));      // Not deleted
        long event02 = createOutboxEvent(DONE, now.minus(Duration.ofDays(9)));      // Not deleted
        long event03 = createOutboxEvent(DONE, now.minus(Duration.ofDays(11)));     // To be deleted
        long event04 = createOutboxEvent(DONE, now.minus(Duration.ofDays(12)));     // To be deleted

        // Error events ================================================================================================
        long event05 = createOutboxEvent(ERROR, now.minus(Duration.ofDays(98)));     // Not deleted
        long event06 = createOutboxEvent(ERROR, now.minus(Duration.ofDays(99)));     // Not deleted
        long event07 = createOutboxEvent(ERROR, now.minus(Duration.ofDays(101)));    // To be deleted
        long event08 = createOutboxEvent(ERROR, now.minus(Duration.ofDays(102)));    // To be deleted

        // Purge =======================================================================================================
        outboxPurgeBatch.purge();

        // Verification ================================================================================================
        assertThat(outboxEventRepository.findById(event01)).isPresent();
        assertThat(outboxEventRepository.findById(event02)).isPresent();
        assertThat(outboxEventRepository.findById(event03)).isNotPresent();
        assertThat(outboxEventRepository.findById(event04)).isNotPresent();
        assertThat(outboxEventRepository.findById(event05)).isPresent();
        assertThat(outboxEventRepository.findById(event06)).isPresent();
        assertThat(outboxEventRepository.findById(event07)).isNotPresent();
        assertThat(outboxEventRepository.findById(event08)).isNotPresent();
    }

    /**
     * Creates an outbox event with the specified processed time and DONE status.
     *
     * @param processedAt the processed time of the outbox event
     * @return the ID of the created outbox event
     */
    private long createOutboxEvent(final OutboxEventStatus eventStatus, final Instant processedAt) {
        return outboxEventRepository.save(
                OutboxEvent.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType(NEW_PAYMENT_STEP)
                        .payload("{}")
                        .status(eventStatus)
                        .processedAt(processedAt)
                        .build()
        ).getId();
    }

}
