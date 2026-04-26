package software.spool.core.adapter.memory;

import software.spool.core.exception.EventBrokerEmitException;
import software.spool.core.exception.EventBrokerListenException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.BrokerMessage;
import software.spool.core.port.bus.Destination;
import software.spool.core.port.bus.EventBroker;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.bus.Subscription;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread-safe in-memory {@link EventBroker} implementation for local
 * testing and single-process deployments.
 *
 * <p>
 * Handlers are stored in a {@link ConcurrentHashMap} keyed by destination and
 * dispatched synchronously on the calling thread.
 * </p>
 */
public class InMemoryEventBroker implements EventBroker {

    private final ConcurrentHashMap<Destination, CopyOnWriteArrayList<Handler<?>>> registry =
            new ConcurrentHashMap<>();

    @Override
    public <E extends Event> Subscription subscribe(
            Destination destination,
            Class<E> eventType,
            Handler<BrokerMessage<E>> handler
    ) throws EventBrokerListenException {

        registry.computeIfAbsent(destination, ignored -> new CopyOnWriteArrayList<>())
                .add(handler);

        return new InMemorySubscription(
                () -> registry.getOrDefault(destination, new CopyOnWriteArrayList<>()).remove(handler)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Event> void publish(
            Destination destination,
            BrokerMessage<E> message
    ) throws EventBrokerEmitException {
        try {
            List<Handler<?>> handlers = registry.getOrDefault(
                    destination,
                    new CopyOnWriteArrayList<>()
            );

            for (Handler<?> handler : handlers) {
                ((Handler<BrokerMessage<E>>) handler).handle(message);
            }
        } catch (Exception e) {
            throw new EventBrokerEmitException(
                    message.payload(),
                    "Failed to publish event to destination [" + destination.value() + "]",
                    e
            );
        }
    }
}