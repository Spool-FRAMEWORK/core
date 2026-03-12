package software.spool.core.port;

import software.spool.core.control.Handler;
import software.spool.core.exception.EventBusListenException;
import software.spool.core.model.Event;

/**
 * Input port for subscribing to events on the event bus.
 *
 * @see software.spool.core.port.decorator.SafeEventBusListener
 */
public interface EventBusListener {
    /**
     * Registers a handler for the given event type.
     *
     * @param <E>     the event type
     * @param event   the class of the event to subscribe to
     * @param handler the handler invoked when a matching event is emitted
     * @return a {@link Subscription} that can be cancelled to unsubscribe
     * @throws EventBusListenException if the subscription fails
     */
    <E extends Event> Subscription on(Class<E> event, Handler<E> handler) throws EventBusListenException;
}
