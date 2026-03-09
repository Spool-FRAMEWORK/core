package software.spool.core.port;

import software.spool.core.exception.EventBusEmitException;
import software.spool.core.model.Event;

public interface EventBusEmitter {
    void emit(Event event) throws EventBusEmitException;
}
