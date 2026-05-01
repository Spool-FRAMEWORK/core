package software.spool.core.adapter.spi;

import software.spool.core.adapter.kafka.KafkaEventBusProvider;
import software.spool.core.spi.bus.EventBusRegistry;

public final class InfraBootstrap {
    public static void init() {
        EventBusRegistry.register(new KafkaEventBusProvider());
    }
}