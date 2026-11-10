package software.spool.core.adapter.memory;

import software.spool.core.exception.EventBusEmitException;
import software.spool.core.exception.EventBusSubscriptionException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.EventBus;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.bus.Subscription;
import software.spool.core.utils.routing.AnnotationEventRouter;
import software.spool.core.utils.routing.EventRouter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe in-memory {@link EventBus} implementation for local
 * testing and single-process deployments.
 *
 * <p>
 * Handlers are stored in a {@link ConcurrentHashMap} keyed by event type and
 * dispatched synchronously on the calling thread. An event with
 * {@code EVERY_SUBSCRIBER} reaches every handler of its type; one with
 * {@code SINGLE_SUBSCRIBER} reaches one of them, taking turns.
 * </p>
 */
public class InMemoryEventBus implements EventBus {
    private final ConcurrentHashMap<Class<? extends Event>, CopyOnWriteArrayList<Handler<?>>> registry =
            new ConcurrentHashMap<>();
    private final Map<Class<? extends Event>, AtomicInteger> nextHandlerByType = new ConcurrentHashMap<>();
    private final EventRouter router;

    public InMemoryEventBus(EventRouter router) {
        this.router = router;
    }

    public InMemoryEventBus() {
        this(new AnnotationEventRouter());
    }

    @Override
    public <E extends Event> Subscription subscribe(
            Class<E> eventType,
            Handler<E> handler
    ) throws EventBusSubscriptionException {
        registry.computeIfAbsent(eventType, ignored -> new CopyOnWriteArrayList<>())
                .add(handler);
        return new InMemorySubscription(
                () -> registry.getOrDefault(eventType, new CopyOnWriteArrayList<>()).remove(handler)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E extends Event> void publish(E event) throws EventBusEmitException {
        try {
            List<Handler<?>> handlers = registry.getOrDefault(
                    event.getClass(),
                    new CopyOnWriteArrayList<>()
            );
            switch (router.routeOf(event.getClass()).deliveryPolicy()) {
                case EVERY_SUBSCRIBER -> handlers.forEach(handler -> ((Handler<E>) handler).handle(event));
                case SINGLE_SUBSCRIBER -> nextHandler(event.getClass(), handlers)
                        .ifPresent(handler -> ((Handler<E>) handler).handle(event));
            }
        } catch (Exception e) {
            throw new EventBusEmitException(
                    event,
                    "Failed to publish event to destination [" + router.routeOf(event.getClass()).channel().value() + "]; " + e.getMessage(),
                    e
            );
        }
    }

    private Optional<Handler<?>> nextHandler(Class<? extends Event> eventType, List<Handler<?>> handlers) {
        if (handlers.isEmpty()) return Optional.empty();
        int position = nextHandlerByType.computeIfAbsent(eventType, type -> new AtomicInteger()).getAndIncrement();
        return Optional.of(handlers.get(Math.floorMod(position, handlers.size())));
    }
}