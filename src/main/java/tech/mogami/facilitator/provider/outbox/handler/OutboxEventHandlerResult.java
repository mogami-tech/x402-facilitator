package tech.mogami.facilitator.provider.outbox.handler;

/**
 * Result for an outbox event handler execution.
 *
 * @param success      whether the handling was successful
 * @param errorMessage error message in case of failure
 */
public record OutboxEventHandlerResult(
        boolean success,
        String errorMessage
) {

    /**
     * Creates a successful response.
     *
     * @return the successful response
     */
    public static OutboxEventHandlerResult ok() {
        return new OutboxEventHandlerResult(true, null);
    }

    /**
     * Creates an error response with the given message.
     *
     * @param message the error message
     * @return the error response
     */
    public static OutboxEventHandlerResult error(final String message) {
        return new OutboxEventHandlerResult(false, message);
    }

}
