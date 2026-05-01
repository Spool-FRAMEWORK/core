package software.spool.core.spi.bus;

import software.spool.core.port.bus.EventBus;

public interface EventBusProvider {
    String name();
    int priority();
    boolean supports(String url);
    EventBus create(String url);
}