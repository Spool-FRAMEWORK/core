package software.spool.core.utils.routing;

import software.spool.core.model.Event;

public interface EventRouter {
    EventRoute routeOf(Class<? extends Event> eventType);
}
