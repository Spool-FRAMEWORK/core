package software.spool.core.port.decorator;

import software.spool.core.port.bus.EventPublisher;
import software.spool.core.exception.EventBusEmitException;
import software.spool.core.model.Event;
import software.spool.core.port.tracing.TracedEventBus;
import software.spool.core.port.tracing.TraceScope;

public class TraceEventPublisher implements EventPublisher {
    private final EventPublisher bus;
    private final TracedEventBus tracer;

    public TraceEventPublisher(EventPublisher bus, TracedEventBus tracer) {
        this.bus = bus;
        this.tracer = tracer;
    }

    public static Builder of(EventPublisher bus) {
        return new Builder(bus);
    }

    @Override
    public void publish(Event event) throws EventBusEmitException {
        TraceScope scope = tracer.send(event);
        try {
            bus.publish(event);
        } catch (Exception e) {
            scope.error(e);
            throw e;
        } finally {
            scope.close();
        }
    }

    public static class Builder {
        private final EventPublisher bus;

        public Builder(EventPublisher bus) {
            this.bus = bus;
        }

        public EventPublisher with(TracedEventBus tracer) {
            return new TraceEventPublisher(bus, tracer);
        }
    }
}
