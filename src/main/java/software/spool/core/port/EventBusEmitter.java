package software.spool.core.port;

import software.spool.core.exception.EventBusEmitException;
import software.spool.core.model.Event;

/**
 * Output port for publishing events to the event bus.
 *
 * @see software.spool.core.port.decorator.SafeEventBusEmitter
 */
public interface EventBusEmitter {
    /**
     * Emits the given event to all registered handlers.
     *
     * @param event the event to emit; must not be {@code null}
     * @throws EventBusEmitException if the emission fails
     */
    void emit(Event event) throws EventBusEmitException;
}
