package software.spool.core.utils.routing;

import software.spool.core.model.Event;
import software.spool.core.model.SpoolEvent;
import software.spool.core.model.annotation.EventAddress;

public final class EventDescriptor {

    public static String address(Class<? extends Event> type) {
        EventAddress ann = type.getAnnotation(EventAddress.class);
        return ann != null
                ? ann.value()
                : type.getSimpleName();
    }

    public static boolean isInternal(Class<? extends Event> type) {
        return SpoolEvent.class.isAssignableFrom(type);
    }
}