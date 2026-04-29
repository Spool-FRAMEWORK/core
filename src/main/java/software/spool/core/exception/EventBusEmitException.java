package software.spool.core.exception;

import software.spool.core.model.Event;

/**
 * Thrown when emitting an event to the event bus fails.
 */
public class EventBusEmitException extends SpoolException {
    private final Event event;

    public EventBusEmitException(Event event, String message) {
        super("Error occurred while emitting to event bus: " + event + ". " + message);
        this.event = event;
    }

    public EventBusEmitException(Event event, String message, Throwable cause) {
        super("Error occurred while emitting to event bus: " + event + ". " + message, cause);
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }
}
