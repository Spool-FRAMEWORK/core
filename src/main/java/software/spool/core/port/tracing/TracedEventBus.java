package software.spool.core.port.tracing;

import software.spool.core.model.Event;

public interface TracedEventBus {
    <E extends Event> TraceScope send(E event);
}
