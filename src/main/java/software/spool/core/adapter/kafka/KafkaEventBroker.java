package software.spool.core.adapter.kafka;

import software.spool.core.exception.EventBusEmitException;
import software.spool.core.exception.EventBusListenException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.*;

public class KafkaEventBroker implements EventBroker {
    private final EventPublisher emitter;
    private final EventSubscriber listener;

    public KafkaEventBroker(EventPublisher emitter, EventSubscriber listener) {
        this.emitter = emitter;
        this.listener = listener;
    }

    @Override
    public void publish(Event event) throws EventBusEmitException {
        emitter.publish(event);
    }

    @Override
    public <E extends Event> Subscription on(Class<E> event, Handler<E> handler) throws EventBusListenException {
        return listener.on(event, handler);
    }
}
