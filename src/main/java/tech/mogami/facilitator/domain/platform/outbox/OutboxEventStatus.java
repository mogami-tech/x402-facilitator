package tech.mogami.facilitator.domain.platform.outbox;

/**
 * Enumeration representing the status of an outbox event.
 */
public enum OutboxEventStatus {

    /** The event is pending processing. */
    PENDING,

    /** The event is currently being processed. */
    PROCESSING,

    /** The event has been successfully processed. */
    DONE,

    /** An error occurred while processing the event. */
    ERROR

}
