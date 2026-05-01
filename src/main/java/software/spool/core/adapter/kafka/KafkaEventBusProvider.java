package software.spool.core.adapter.kafka;

import software.spool.core.port.bus.EventBus;
import software.spool.core.spi.bus.EventBusProvider;

public class KafkaEventBusProvider implements EventBusProvider {
    @Override
    public String name() {
        return "KAFKA";
    }

    @Override
    public int priority() {
        return 10;
    }

    @Override
    public boolean supports(String url) {
        return url != null;
    }

    @Override
    public EventBus create(String url) {
        return new KafkaEventBus(new KafkaEventBusConfig(url));
    }
}