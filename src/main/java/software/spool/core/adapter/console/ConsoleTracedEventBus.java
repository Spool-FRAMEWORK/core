package software.spool.core.adapter.console;

import software.spool.core.model.Event;
import software.spool.core.port.tracing.TracedEventBus;
import software.spool.core.port.tracing.TraceScope;

public class ConsoleTracedEventBus implements TracedEventBus {
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
