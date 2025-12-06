package tech.mogami.facilitator.provider.outbox.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEventType;
import tech.mogami.facilitator.provider.outbox.batch.OutboxBatch;
import tech.mogami.facilitator.provider.outbox.handler.OutboxEventHandler;
import tech.mogami.facilitator.provider.outbox.service.OutboxService;

import java.util.List;
import java.util.Map;

import static tech.mogami.facilitator.domain.platform.outbox.OutboxEventType.NEW_PAYMENT_STEP;

/**
 * Batch for processing new payment step outbox events.
 */
@Slf4j
@Component
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class NewPaymentStepBatch extends OutboxBatch {

    /**
     * Constructor for NewPaymentStepBatch.
     *
     * @param handlers      Map of outbox event handlers
     * @param outboxService Outbox service
     */
    public NewPaymentStepBatch(
            final Map<OutboxEventType, OutboxEventHandler<?>> handlers,
            final OutboxService outboxService
    ) {
        super(handlers, outboxService);
    }

    @Override
    protected List<OutboxEventType> supportedTypes() {
        return List.of(NEW_PAYMENT_STEP);
    }

}
