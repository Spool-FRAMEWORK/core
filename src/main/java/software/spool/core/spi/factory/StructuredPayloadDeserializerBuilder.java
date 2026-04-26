package software.spool.core.spi.factory;

import com.fasterxml.jackson.databind.JsonNode;
import software.spool.core.port.serde.NamingConvention;
import software.spool.core.port.serde.PayloadDeserializer;

import java.util.List;
import java.util.Map;

public interface StructuredPayloadDeserializerBuilder {
    StructuredPayloadDeserializerBuilder convention(NamingConvention convention);
    <D> PayloadDeserializer<D> as(Class<D> type);
    <D> PayloadDeserializer<List<D>> asList(Class<D> type);
    PayloadDeserializer<JsonNode> asNode();
    PayloadDeserializer<Map<String, Object>> asMap();
}