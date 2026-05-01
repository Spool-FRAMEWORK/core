package software.spool.core.spi;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class AbstractServiceRegistry<T> {

    protected Map<String, T> load(Class<T> type, Function<T, String> keyExtractor) {
        Map<String, T> map = new ConcurrentHashMap<>();

        ServiceLoader<T> loader = ServiceLoader.load(type);
        for (T service : loader) {
            map.put(keyExtractor.apply(service).toUpperCase(), service);
        }

        return map;
    }
}