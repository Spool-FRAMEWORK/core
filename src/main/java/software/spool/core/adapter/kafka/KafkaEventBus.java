package software.spool.core.adapter.kafka;

import software.spool.core.exception.EventBusEmitException;
import software.spool.core.exception.EventBusSubscriptionException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.*;

public class KafkaEventBus implements EventBus {
    private final EventPublisher publisher;
    private final EventSubscriber subscriber;

    public KafkaEventBus(EventPublisher publisher, EventSubscriber subscriber) {
        this.publisher = publisher;
        this.subscriber = subscriber;
    }

    @Override
    public <E extends Event> void publish(E event) throws EventBusEmitException {
        publisher.publish(event);
    }

    @Override
    public <E extends Event> Subscription subscribe(Class<E> eventType, Handler<E> handler) throws EventBusSubscriptionException {
        return subscriber.subscribe(eventType, handler);
    }
}
