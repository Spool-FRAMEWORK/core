package software.spool.core.port.bus;

import software.spool.core.exception.EventBusListenException;
import software.spool.core.model.Event;

@FunctionalInterface
public interface EventSubscriber {
    <E extends Event> Subscription subscribe(
            Destination destination,
            Class<E> eventType,
            Handler<E> handler
    ) throws EventBusListenException;
}
