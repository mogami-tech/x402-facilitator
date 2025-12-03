package tech.mogami.facilitator.provider.outbox.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.mogami.facilitator.domain.platform.outbox.OutboxEventType;
import tech.mogami.facilitator.provider.outbox.batch.OutboxBatch;
import tech.mogami.facilitator.provider.outbox.handler.OutboxEventHandler;
import tech.mogami.facilitator.provider.outbox.service.OutboxService;

import java.util.List;

import static tech.mogami.facilitator.domain.platform.outbox.OutboxEventType.NEW_PAYMENT_STEP;

/**
 * Batch for processing new payment step outbox events.
 */
@Slf4j
@Component
@SuppressWarnings({"checkstyle:DesignForExtension", "unused"})
public class NewPaymentStepBatch extends OutboxBatch {

    /**
     * Constructor.
     *
     * @param newOutboxService      the outbox service
     * @param newDiscoveredHandlers the list of discovered outbox event handlers
     */
    public NewPaymentStepBatch(final OutboxService newOutboxService,
                               final List<OutboxEventHandler<?>> newDiscoveredHandlers) {
        super(newOutboxService, newDiscoveredHandlers);
    }

    @Override
    protected List<OutboxEventType> supportedTypes() {
        return List.of(NEW_PAYMENT_STEP);
    }

}
