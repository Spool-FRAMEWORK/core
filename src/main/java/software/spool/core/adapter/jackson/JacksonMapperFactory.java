package software.spool.core.adapter.jackson;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

final class JacksonMapperFactory {

    private static final ObjectMapper JSON_MAPPER = buildMapper(
            JsonMapper.builder().enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build()
    );

    private static final ObjectMapper YAML_MAPPER = buildMapper(
            JsonMapper.builder(new YAMLFactory()).enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS).build()
    );

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