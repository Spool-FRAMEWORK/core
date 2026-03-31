package software.spool.core.adapter.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.spool.core.exception.DeserializationException;
import software.spool.core.port.serde.NamingConvention;
import software.spool.core.port.serde.PayloadDeserializer;

import java.util.List;
import java.util.Map;

import static software.spool.core.adapter.jackson.PayloadDeserializerFactory.toJacksonStrategy;

public final class StructuredDeserializerBuilder {
    private final ObjectMapper mapper;

    StructuredDeserializerBuilder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public StructuredDeserializerBuilder convention(NamingConvention convention) {
        return new StructuredDeserializerBuilder(mapper.copy()
                .setPropertyNamingStrategy(toJacksonStrategy(convention)));
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