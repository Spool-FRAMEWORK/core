package software.spool.core;

import software.spool.core.adapter.spi.InfraBootstrap;
import software.spool.core.spi.bus.EventBusRegistry;

public class Main {
    public static void main(String[] args) {
        EventBusRegistry.get("KAFKA", "host.docker.internal.localhost:9092");
    }
}
