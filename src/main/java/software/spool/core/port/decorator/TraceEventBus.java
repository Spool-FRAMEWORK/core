package software.spool.core.port.decorator;

import software.spool.core.port.bus.Handler;
import software.spool.core.exception.EventBusEmitException;
import software.spool.core.exception.EventBusListenException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.EventBus;
import software.spool.core.port.tracing.TracedEventBus;
import software.spool.core.port.bus.Subscription;
import software.spool.core.port.tracing.TraceScope;

public class TraceEventBus implements EventBus {
    private final EventBus bus;
    private final TracedEventBus tracer;

    public TraceEventBus(EventBus bus, TracedEventBus tracer) {
        this.bus = bus;
        this.tracer = tracer;
    }

    public static Builder of(EventBus bus) {
        return new Builder(bus);
    }

    @Override
    public void emit(Event event) throws EventBusEmitException {
        TraceScope scope = tracer.send(event);
        try {
            bus.emit(event);
        } catch (Exception e) {
            scope.error(e);
            throw e;
        } finally {
            scope.close();
        }
    }

    @Override
    public <E extends Event> Subscription on(Class<E> event, Handler<E> handler) throws EventBusListenException {
        return bus.on(event, handler);
    }

    public static class Builder {
        private final EventBus bus;

        public Builder(EventBus bus) {
            this.bus = bus;
        }

        public EventBus with(TracedEventBus tracer) {
            return new TraceEventBus(bus, tracer);
        }
    }
}
