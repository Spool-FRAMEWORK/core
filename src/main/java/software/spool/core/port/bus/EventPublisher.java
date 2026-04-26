package software.spool.core.port.bus;

import software.spool.core.exception.EventBusEmitException;
import software.spool.core.model.Event;
import software.spool.core.port.decorator.SafeEventPublisher;

public interface EventPublisher {
    <E extends Event> void publish(Destination destination, BrokerMessage<E> message) throws EventBusEmitException;
}
