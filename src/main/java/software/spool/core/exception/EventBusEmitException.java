package software.spool.core.exception;

import software.spool.core.model.Event;

public class EventBusEmitException extends SpoolException {
    private final Event event;

    public EventBusEmitException(Event event, String message) {
        super("Error occurred while emitting to event bus: " + message);
        this.event = event;
    }
    public EventBusEmitException(String message, Throwable cause) {
        super("Error occurred while emitting to event bus: " + message, cause);
        this.event = null;
    }

    public Event getEvent() {
        return event;
    }
}
