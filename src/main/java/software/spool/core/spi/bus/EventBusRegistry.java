package software.spool.core.spi.bus;

import software.spool.core.port.bus.EventBus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EventBusRegistry {
    private static final Map<String, List<EventBusProvider>> PROVIDERS = new ConcurrentHashMap<>();

    private EventBusRegistry() {}

    public static void register(EventBusProvider provider) {
        PROVIDERS.computeIfAbsent(provider.name().toUpperCase(), k -> new ArrayList<>())
                .add(provider);
        PROVIDERS.get(provider.name().toUpperCase())
                .sort(Comparator.comparingInt(EventBusProvider::priority).reversed());
    }

    public static EventBus get(String name, String url) {
        List<EventBusProvider> providers = PROVIDERS.get(name.toUpperCase());
        if (providers == null)
            throw new IllegalArgumentException("No provider for: " + name);
        return providers.stream()
                .filter(p -> p.supports(url))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("No compatible provider for: " + name)
                )
                .create(url);
    }
}