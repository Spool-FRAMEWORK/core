package software.spool.core.port.bus;

import software.spool.core.exception.EventBusEmitException;
import software.spool.core.model.Event;
import software.spool.core.port.decorator.SafeEventPublisher;

/**
 * Output port for publishing events to the event bus.
 *
 * @see SafeEventPublisher
 */
public interface EventPublisher {
    /**
     * Emits the given event to all registered handlers.
     *
     * @param event the event to emit; must not be {@code null}
     * @throws EventBusEmitException if the emission fails
     */
    void emit(Event event) throws EventBusEmitException;
}
