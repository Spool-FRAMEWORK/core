package software.spool.core.adapter.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
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
    private static final ObjectMapper CAMEL = new ObjectMapper().findAndRegisterModules();
    private static final ObjectMapper SNAKE = new ObjectMapper().findAndRegisterModules()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    private static final ObjectMapper PASCAL = new ObjectMapper().findAndRegisterModules()
            .setPropertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE);
    private static final ObjectMapper KEBAB = new ObjectMapper().findAndRegisterModules()
            .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);

    public static <D> PayloadDeserializer<D> camelCase(Class<D> type) {
        return raw -> {
            try {
                return CAMEL.readValue(raw, type);
            } catch (JsonProcessingException e) {
                throw new DeserializationException(raw, e);
            }
        };
    }

    public static <D> PayloadDeserializer<D> snakeCase(Class<D> type) {
        return raw -> {
            try {
                return SNAKE.readValue(raw, type);
            } catch (JsonProcessingException e) {
                throw new DeserializationException(raw, e);
            }
        };
    }

    public static <D> PayloadDeserializer<D> pascalCase(Class<D> type) {
        return raw -> {
            try {
                return PASCAL.readValue(raw, type);
            } catch (JsonProcessingException e) {
                throw new DeserializationException(raw, e);
            }
        };
    }

    public static <D> PayloadDeserializer<D> kebabCase(Class<D> type) {
        return raw -> {
            try {
                return KEBAB.readValue(raw, type);
            } catch (JsonProcessingException e) {
                throw new DeserializationException(raw, e);
            }
        };
    }

    private DomainMapperFactory() {
    }
}
