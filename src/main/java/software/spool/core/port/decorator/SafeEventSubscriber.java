package software.spool.core.port.decorator;

import software.spool.core.port.bus.*;
import software.spool.core.exception.EventBrokerListenException;
import software.spool.core.exception.SpoolException;
import software.spool.core.model.Event;

/**
 * Decorator that wraps an {@link EventSubscriber} and normalises any
 * unchecked exception into an {@link EventBrokerListenException}.
 *
 * <p>
 * Exceptions that are already a {@link SpoolException} are rethrown as-is.
 * </p>
 */
public class SafeEventSubscriber implements EventSubscriber {
    private final EventSubscriber listener;

    public SafeEventSubscriber(EventSubscriber listener) {
        this.listener = listener;
    }

    @Override
    public <E extends Event> Subscription subscribe(Destination destination, Class<E> eventType, Handler<BrokerMessage<E>> handler) throws EventBrokerListenException {
        try {
            return listener.subscribe(destination, eventType, handler);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new EventBrokerListenException(eventType, e.getMessage(), e);
        }
    }

    public static SafeEventSubscriber of(EventSubscriber listener) {
        return new SafeEventSubscriber(listener);
    }
}
