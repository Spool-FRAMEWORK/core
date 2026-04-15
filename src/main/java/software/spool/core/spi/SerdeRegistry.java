package software.spool.core.spi;

import software.spool.core.spi.factory.StructuredFormatProvider;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public final class SerdeRegistry {
    private static final Map<String, StructuredFormatProvider> STRUCTURED_FORMATS = new ConcurrentHashMap<>();

    static {
        ServiceLoader<StructuredFormatProvider> loader = ServiceLoader.load(StructuredFormatProvider.class);
        for (StructuredFormatProvider provider : loader) {
            STRUCTURED_FORMATS.put(provider.getName().toUpperCase(), provider);
        }
    }

    private SerdeRegistry() {}

    public static StructuredFormatProvider structured(String format) {
        StructuredFormatProvider provider = STRUCTURED_FORMATS.get(format.toUpperCase());
        if (provider == null) {
            throw new IllegalArgumentException(
                    "No StructuredFormatProvider found for format: '" + format + "'. " +
                            "Are you missing a dependency in your classpath?"
            );
        }
        return provider;
    }
}