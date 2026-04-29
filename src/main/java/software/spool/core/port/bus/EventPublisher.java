package software.spool.core.port.bus;

import software.spool.core.exception.EventBusEmitException;
import software.spool.core.model.Event;

public interface EventPublisher {
    <E extends Event> void publish(Event event) throws EventBusEmitException;
}
