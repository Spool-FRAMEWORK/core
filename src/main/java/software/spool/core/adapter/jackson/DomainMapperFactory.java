package software.spool.core.adapter.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import software.spool.core.exception.DeserializationException;
import software.spool.core.port.serde.PayloadDeserializer;

/**
 * Factory methods for {@link PayloadDeserializer} instances that map JSON
 * payloads to Java types using different property naming strategies.
 *
 * <p>
 * Each method returns a deserializer backed by a shared, thread-safe
 * Jackson {@link ObjectMapper} configured with the corresponding
 * {@link com.fasterxml.jackson.databind.PropertyNamingStrategies} strategy.
 * </p>
 */
public final class DomainMapperFactory {
    private static final ObjectMapper CAMEL = JacksonMapperFactory.json().copy();
    private static final ObjectMapper SNAKE = JacksonMapperFactory.json().copy()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    private static final ObjectMapper PASCAL = JacksonMapperFactory.json().copy()
            .setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE);
    private static final ObjectMapper KEBAB = JacksonMapperFactory.json().copy()
            .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

    public static <D> PayloadDeserializer<D> camelCase(Class<D> type) {
        return raw -> {
            try {
                return CAMEL.readValue(raw, type);
            } catch (Exception e) {
                throw new DeserializationException(new String(raw), e);
            }
        };
    }

    public static <D> PayloadDeserializer<D> snakeCase(Class<D> type) {
        return raw -> {
            try {
                return SNAKE.readValue(raw, type);
            } catch (Exception e) {
                throw new DeserializationException(new String(raw), e);
            }
        };
    }

    public static <D> PayloadDeserializer<D> pascalCase(Class<D> type) {
        return raw -> {
            try {
                return PASCAL.readValue(raw, type);
            } catch (Exception e) {
                throw new DeserializationException(new String(raw), e);
            }
        };
    }

    public static <D> PayloadDeserializer<D> kebabCase(Class<D> type) {
        return raw -> {
            try {
                return KEBAB.readValue(raw, type);
            } catch (Exception e) {
                throw new DeserializationException(new String(raw), e);
            }
        };
    }

    private DomainMapperFactory() {
    }
}
