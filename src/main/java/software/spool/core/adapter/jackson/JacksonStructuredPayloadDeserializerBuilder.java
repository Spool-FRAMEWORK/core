package software.spool.core.adapter.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import software.spool.core.exception.DeserializationException;
import software.spool.core.port.serde.NamingConvention;
import software.spool.core.port.serde.PayloadDeserializer;
import software.spool.core.spi.factory.StructuredPayloadDeserializerBuilder;

import java.util.List;
import java.util.Map;

public final class JacksonStructuredPayloadDeserializerBuilder implements StructuredPayloadDeserializerBuilder {
    private final ObjectMapper mapper;

    JacksonStructuredPayloadDeserializerBuilder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JacksonStructuredPayloadDeserializerBuilder convention(NamingConvention convention) {
        return new JacksonStructuredPayloadDeserializerBuilder(mapper.copy()
                .setPropertyNamingStrategy(toJacksonStrategy(convention)));
    }

    static PropertyNamingStrategy toJacksonStrategy(NamingConvention convention) {
        return switch (convention) {
            case CAMEL_CASE  -> PropertyNamingStrategies.LOWER_CAMEL_CASE;
            case SNAKE_CASE  -> PropertyNamingStrategies.SNAKE_CASE;
            case PASCAL_CASE -> PropertyNamingStrategies.UPPER_CAMEL_CASE;
            case KEBAB_CASE  -> PropertyNamingStrategies.KEBAB_CASE;
        };
    }

    public <D> PayloadDeserializer<D> as(Class<D> type) {
        return raw -> {
            try {
                return mapper.readValue(raw, type);
            } catch (JsonProcessingException e) {
                throw new DeserializationException(raw, e.getMessage());
            }
        };
    }

    public <D> PayloadDeserializer<List<D>> asList(Class<D> type) {
        return raw -> {
            try {
                JavaType listType = mapper.getTypeFactory()
                        .constructCollectionType(List.class, type);
                return mapper.readValue(raw, listType);
            } catch (JsonProcessingException e) {
                throw new DeserializationException(raw, e.getMessage());
            }
        };
    }

    public PayloadDeserializer<JsonNode> asNode() {
        return raw -> {
            try {
                return mapper.readTree(raw);
            } catch (Exception e) {
                throw new DeserializationException(raw, e.getMessage());
            }
        };
    }

    public PayloadDeserializer<Map<String, Object>> asMap() {
        return raw -> {
            try {
                return mapper.readValue(raw, new TypeReference<>() {});
            } catch (Exception e) {
                throw new DeserializationException(raw, e.getMessage());
            }
        };
    }
}