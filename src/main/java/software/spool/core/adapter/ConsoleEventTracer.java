package software.spool.core.adapter;

import software.spool.core.model.Event;
import software.spool.core.port.EventTracer;
import software.spool.core.port.TraceScope;

public class ConsoleEventTracer implements EventTracer {
    @Override
    public <E extends Event> TraceScope send(E event) {
        System.out.println(event);
        return new TraceScope() {
            @Override public void error(Exception error) {
                System.err.println(error.getMessage());
            }
            @Override public void end() {}
        };
    }
}
