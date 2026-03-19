package software.spool.core.port;

import software.spool.core.model.Event;

public interface EventTracer {
    <E extends Event> TraceScope send(E event);
}
