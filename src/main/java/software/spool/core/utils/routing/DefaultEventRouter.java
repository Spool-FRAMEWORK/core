package software.spool.core.utils.routing;

import software.spool.core.model.Event;

public class DefaultEventRouter implements EventRouter {
    @Override
    public String resolve(Class<? extends Event> type) {
        String base = EventDescriptor.address(type);
        if (EventDescriptor.isInternal(type)) return "spool." + base;
        return base;
    }
}