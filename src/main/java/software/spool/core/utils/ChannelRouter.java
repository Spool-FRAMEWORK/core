package software.spool.core.utils;

import software.spool.core.model.Event;

import java.util.HashMap;
import java.util.Map;

public class ChannelRouter {
    private final Map<Class<? extends Event>, String> routes;
    private final ChannelNamingConvention convention;

    private ChannelRouter(ChannelNamingConvention convention) {
        this.routes = new HashMap<>();
        this.convention = convention;
    }

    public static ChannelRouter withConvention(ChannelNamingConvention convention) {
        return new ChannelRouter(convention);
    }

    public static ChannelRouter defaults() {
        return new ChannelRouter(ChannelNamingConvention.DOT_CASE);
    }

    public <T extends Event> ChannelRouter route(Class<T> eventType, String channel) {
        routes.put(eventType, channel);
        return this;
    }

    public String resolve(Event event) {
        return routes.getOrDefault(event.getClass(), convention.apply(event.getClass()));
    }
}
