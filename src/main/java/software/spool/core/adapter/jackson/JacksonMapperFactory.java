package software.spool.core.adapter.jackson;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

final class JacksonMapperFactory {

    private static final ObjectMapper JSON_MAPPER = buildMapper(new ObjectMapper());
    private static final ObjectMapper YAML_MAPPER = buildMapper(new ObjectMapper(new YAMLFactory()));

    static ObjectMapper json() {
        return JSON_MAPPER;
    }

    static ObjectMapper yaml() {
        return YAML_MAPPER;
    }

    private static ObjectMapper buildMapper(ObjectMapper mapper) {
        return mapper
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private JacksonMapperFactory() {
    }
}