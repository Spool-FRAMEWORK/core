package software.spool.core.adapter.kafka;

import software.spool.core.exception.EventBusEmitException;
import software.spool.core.exception.EventBusListenException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.*;

public class KafkaEventBus implements EventBus {
    private final EventBusEmitter emitter;
    private final EventBusListener listener;

    public KafkaEventBus(EventBusEmitter emitter, EventBusListener listener) {
        this.emitter = emitter;
        this.listener = listener;
    }

    @Override
    public void emit(Event event) throws EventBusEmitException {
        emitter.emit(event);
    }

    @Override
    public <E extends Event> Subscription on(Class<E> event, Handler<E> handler) throws EventBusListenException {
        return listener.on(event, handler);
    }
}
