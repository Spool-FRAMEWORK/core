package software.spool.core.utils.routing;

import software.spool.core.model.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps event types to named channels.
 *
 * <p>
 * If no explicit route is registered for an event type, the configured
 * {@link ChannelNamingConvention} is used to derive a default channel name
 * from the event class name.
 * </p>
 *
 * <pre>{@code
 * ChannelRouter router = ChannelRouter.defaults()
 *         .route(OrderReceived.class, "orders")
 *         .route(PaymentProcessed.class, "payments");
 *
 * String ch = router.resolve(event); // "orders", "payments", or convention-based default
 * }</pre>
 */
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
