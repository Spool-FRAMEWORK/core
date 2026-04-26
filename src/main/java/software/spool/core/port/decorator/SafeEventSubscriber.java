package software.spool.core.port.decorator;

import software.spool.core.port.bus.Handler;
import software.spool.core.exception.EventBusListenException;
import software.spool.core.exception.SpoolException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.EventSubscriber;
import software.spool.core.port.bus.Subscription;

/**
 * Decorator that wraps an {@link EventSubscriber} and normalises any
 * unchecked exception into an {@link EventBusListenException}.
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
    public <E extends Event> Subscription on(Class<E> event, Handler<E> handler) {
        try {
            return listener.on(event, handler);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new EventBusListenException(event, e.getMessage(), e);
        }
    }

    public static SafeEventSubscriber of(EventSubscriber listener) {
        return new SafeEventSubscriber(listener);
    }
}
