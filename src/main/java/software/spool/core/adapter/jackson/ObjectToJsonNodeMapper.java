package software.spool.core.adapter.jackson;

import com.fasterxml.jackson.databind.JsonNode;

public final class ObjectToJsonNodeMapper {
    private ObjectToJsonNodeMapper() {}

    public static JsonNode map(Object object) {
        return JacksonMapperFactory.json().valueToTree(object);
    }
}