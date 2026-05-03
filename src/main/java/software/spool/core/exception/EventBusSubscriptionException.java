package software.spool.core.exception;

/**
 * Thrown when subscribing to an event type on the event bus fails.
 */
public class EventBusSubscriptionException extends SpoolException {
    private final Class<?> eventType;

    public EventBusSubscriptionException(Class<?> eventType, String message) {
        super("Error occurred while listening on event bus: " + eventType.getSimpleName() + ". " + message);
        this.eventType = eventType;
    }

    public EventBusSubscriptionException(Class<?> eventType, String message, Throwable cause) {
        super("Error occurred while listening on event bus: " + eventType.getSimpleName() + ". " + message, cause);
        this.eventType = eventType;
    }

    public Class<?> getEventType() {
        return eventType;
    }
}
