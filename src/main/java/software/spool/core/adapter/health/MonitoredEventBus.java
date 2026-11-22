package software.spool.core.adapter.health;

import software.spool.core.exception.EventBusEmitException;
import software.spool.core.exception.EventBusSubscriptionException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.EventBus;
import software.spool.core.port.bus.Handler;
import software.spool.core.port.bus.Subscription;
import software.spool.core.port.health.PieceMonitor;

/**
 * Publishes to and subscribes on another bus and tells its {@link PieceMonitor} how each call went. What a handler
 * does with an event is the work of its module, not of the bus.
 */
public final class MonitoredEventBus implements EventBus {
    private final EventBus delegate;
    private final PieceMonitor monitor;

    public MonitoredEventBus(EventBus delegate, PieceMonitor monitor) {
        this.delegate = delegate;
        this.monitor = monitor;
    }

    @Override
    public <E extends Event> void publish(E event) throws EventBusEmitException {
        monitor.run(() -> delegate.publish(event));
    }

    @Override
    public <E extends Event> Subscription subscribe(Class<E> eventType, Handler<E> handler) throws EventBusSubscriptionException {
        return monitor.call(() -> delegate.subscribe(eventType, handler));
    }
}
