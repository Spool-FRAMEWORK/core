package software.spool.core.adapter.kafka;

import software.spool.core.exception.EventBrokerEmitException;
import software.spool.core.exception.EventBrokerListenException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.*;

public class KafkaEventBroker implements EventBroker {
    private final EventPublisher publisher;
    private final EventSubscriber subscriber;

    public KafkaEventBroker(EventPublisher publisher, EventSubscriber subscriber) {
        this.publisher = publisher;
        this.subscriber = subscriber;
    }

    @Override
    public <E extends Event> void publish(Destination destination, BrokerMessage<E> message) throws EventBrokerEmitException {
        publisher.publish(destination, message);
    }

    @Override
    public <E extends Event> Subscription subscribe(Destination destination, Class<E> eventType, Handler<E> handler) throws EventBrokerListenException {
        return subscriber.subscribe(destination, eventType, handler);
    }
}
