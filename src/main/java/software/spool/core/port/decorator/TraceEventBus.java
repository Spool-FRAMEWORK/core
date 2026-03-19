package software.spool.core.port.decorator;

import software.spool.core.control.Handler;
import software.spool.core.exception.EventBusEmitException;
import software.spool.core.exception.EventBusListenException;
import software.spool.core.model.Event;
import software.spool.core.port.EventBus;
import software.spool.core.port.EventTracer;
import software.spool.core.port.Subscription;
import software.spool.core.port.TraceScope;

public class TraceEventBus implements EventBus {
    private final EventBus bus;
    private final EventTracer tracer;

    public TraceEventBus(EventBus bus, EventTracer tracer) {
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

        public EventBus with(EventTracer tracer) {
            return new TraceEventBus(bus, tracer);
        }
    }
}
