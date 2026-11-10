package software.spool.core.port.bus;

import software.spool.core.exception.EventBusSubscriptionException;
import software.spool.core.model.Event;

/**
 * Subscribes a handler to a type of event.
 *
 * <p>
 * Which subscriptions receive each event depends on the {@link DeliveryPolicy} of its route:
 * with {@link DeliveryPolicy#SINGLE_SUBSCRIBER} one subscription of the whole system handles it;
 * with {@link DeliveryPolicy#EVERY_SUBSCRIBER} every active subscription does, replicas included.
 * An event can arrive more than once, so every handler must be idempotent.
 * </p>
 */
@FunctionalInterface
public interface EventSubscriber {
    <E extends Event> Subscription subscribe(
            Class<E> eventType,
            Handler<E> handler
    ) throws EventBusSubscriptionException;
}
