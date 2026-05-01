package software.spool.core.port.bus;

import software.spool.core.exception.EventBusSubscriptionException;
import software.spool.core.model.Event;

@FunctionalInterface
public interface EventSubscriber {
    <E extends Event> Subscription subscribe(
            Class<E> eventType,
            Handler<E> handler
    ) throws EventBusSubscriptionException;
}
