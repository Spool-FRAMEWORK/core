package software.spool.core.port.decorator;

import software.spool.core.port.bus.EventBusEmitter;
import software.spool.core.port.bus.Handler;
import software.spool.core.exception.EventBusEmitException;
import software.spool.core.exception.EventBusListenException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.EventBus;
import software.spool.core.port.tracing.TracedEventBus;
import software.spool.core.port.bus.Subscription;
import software.spool.core.port.tracing.TraceScope;

public class TraceEventBusEmitter implements EventBusEmitter {
    private final EventBusEmitter bus;
    private final TracedEventBus tracer;

    public TraceEventBusEmitter(EventBusEmitter bus, TracedEventBus tracer) {
        this.bus = bus;
        this.tracer = tracer;
    }

    public static Builder of(EventBusEmitter bus) {
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

    public static class Builder {
        private final EventBusEmitter bus;

        public Builder(EventBusEmitter bus) {
            this.bus = bus;
        }

        public EventBusEmitter with(TracedEventBus tracer) {
            return new TraceEventBusEmitter(bus, tracer);
        }
    }
}
