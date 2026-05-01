package software.spool.core.utils.routing;

import software.spool.core.model.Event;

public interface EventRouter {
    String resolve(Class<? extends Event> eventType);
}