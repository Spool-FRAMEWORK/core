package software.spool.core.port.decorator;

import software.spool.core.exception.EventBusEmitException;
import software.spool.core.exception.SpoolException;
import software.spool.core.model.Event;
import software.spool.core.port.EventBusEmitter;

/**
 * Decorator that wraps an {@link EventBusEmitter} and normalises any
 * unchecked exception into an {@link EventBusEmitException}.
 *
 * <p>
 * Exceptions that are already a {@link SpoolException} are rethrown as-is.
 * </p>
 */
public class SafeEventBusEmitter implements EventBusEmitter {
    private final EventBusEmitter emitter;

    public SafeEventBusEmitter(EventBusEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public void emit(Event event) {
        try {
            emitter.emit(event);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new EventBusEmitException(event, e.getMessage(), e);
        }
    }

    public static SafeEventBusEmitter of(EventBusEmitter emitter) {
        return new SafeEventBusEmitter(emitter);
    }
}
