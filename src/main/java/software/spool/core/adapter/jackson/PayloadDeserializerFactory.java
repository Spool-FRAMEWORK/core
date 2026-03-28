package software.spool.core.adapter.jackson;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import software.spool.core.exception.DeserializationException;
import software.spool.core.port.serde.NamingConvention;
import software.spool.core.port.serde.PayloadDeserializer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class PayloadDeserializerFactory {
    private static final ObjectMapper JSON_MAPPER = buildMapper(new ObjectMapper());
    private static final ObjectMapper YAML_MAPPER = buildMapper(new ObjectMapper(new YAMLFactory()));

    private static ObjectMapper buildMapper(ObjectMapper mapper) {
        return mapper
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static StructuredDeserializerBuilder json() {
        return new StructuredDeserializerBuilder(JSON_MAPPER);
    }

    public static StructuredDeserializerBuilder yaml() {
        return new StructuredDeserializerBuilder(YAML_MAPPER);
    }

    public static PayloadDeserializer<List<String>> textLines() {
        return raw -> {
            try {
                return Stream.of(raw.split("\n"))
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .toList();
            } catch (Exception e) {
                throw new DeserializationException(raw, e.getMessage());
            }
        };
    }

    public static PayloadDeserializer<List<Map<String, String>>> csv() {
        return raw -> {
            try {
                String[] headers = raw.split("\n")[0].split(",");
                return Stream.of(raw.split("\n"))
                        .skip(1)
                        .map(line -> {
                            String[] values = line.split(",");
                            Map<String, String> row = new LinkedHashMap<>();
                            for (int i = 0; i < Math.min(headers.length, values.length); i++)
                                row.put(headers[i].trim(), values[i].trim());
                            return row;
                        })
                        .toList();
            } catch (Exception e) {
                throw new DeserializationException(raw, e.getMessage());
            }
        };
    }

    static PropertyNamingStrategy toJacksonStrategy(NamingConvention convention) {
        return switch (convention) {
            case CAMEL_CASE  -> PropertyNamingStrategies.LOWER_CAMEL_CASE;
            case SNAKE_CASE  -> PropertyNamingStrategies.SNAKE_CASE;
            case PASCAL_CASE -> PropertyNamingStrategies.UPPER_CAMEL_CASE;
            case KEBAB_CASE  -> PropertyNamingStrategies.KEBAB_CASE;
        };
    }

    private PayloadDeserializerFactory() {}
}