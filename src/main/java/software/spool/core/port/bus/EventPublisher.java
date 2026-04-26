package software.spool.core.port.bus;

import software.spool.core.exception.EventBrokerEmitException;
import software.spool.core.model.Event;

public interface EventPublisher {
    <E extends Event> void publish(Destination destination, BrokerMessage<E> message) throws EventBrokerEmitException;
}
