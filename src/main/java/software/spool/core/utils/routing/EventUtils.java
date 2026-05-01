package software.spool.core.utils.routing;

import software.spool.core.model.annotation.EventAddress;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EventUtils {
    private static final Map<Class<?>, String> CACHE = new ConcurrentHashMap<>();

    public static String resolveAddress(Class<?> eventClass) {
        return CACHE.computeIfAbsent(eventClass, cls -> {
            EventAddress annotation = cls.getAnnotation(EventAddress.class);
            return annotation != null
                    ? annotation.value()
                    : cls.getSimpleName();
        });
    }
}
