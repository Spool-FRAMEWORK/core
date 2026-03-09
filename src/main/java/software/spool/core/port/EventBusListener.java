package software.spool.core.port;

import software.spool.core.control.Handler;
import software.spool.core.exception.EventBusListenException;
import software.spool.core.model.Event;

public interface EventBusListener {
    <E extends Event> Subscription on(Class<E> event, Handler<E> handler) throws EventBusListenException;
}
