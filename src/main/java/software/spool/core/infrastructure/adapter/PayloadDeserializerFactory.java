package software.spool.core.infrastructure.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import software.spool.core.exception.DeserializationException;
import software.spool.core.port.PayloadDeserializer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Factory methods for common {@link PayloadDeserializer} implementations.
 *
 * <p>
 * All returned deserializers are stateless lambdas backed by shared,
 * thread-safe
 * Jackson {@link ObjectMapper} instances.
 * </p>
 */
public class PayloadDeserializerFactory {
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    /**
     * Returns a deserializer that parses any valid JSON string into a
     * {@link JsonNode} tree.
     *
     * @return a JSON deserializer
     */
    public static PayloadDeserializer<JsonNode> json() {
        return raw -> {
            try {
                return jsonMapper.readTree(raw);
            } catch (Exception e) {
                throw new DeserializationException("Failed to deserialize JSON", e);
            }
        };
    }

    /**
     * Returns a deserializer that parses a JSON <strong>object</strong> string into a
     * {@code Map<String, Object>} preserving JSON value types.
     */
    public static PayloadDeserializer<Map<String, Object>> jsonObject() {
        return raw -> {
            try {
                JsonNode root = jsonMapper.readTree(raw);
                if (!root.isObject())
                    throw new DeserializationException("Expected JSON object", raw);
                return jsonMapper.convertValue(root, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                throw new DeserializationException("Failed to deserialize JSON object", e);
            }
        };
    }


    /**
     * Returns a deserializer that parses a JSON string and validates that its
     * root element is a JSON array.
     *
     * @return a JSON array deserializer
     * @throws DeserializationException if the root element is not a JSON array
     */
    public static PayloadDeserializer<JsonNode> jsonArray() {
        return raw -> {
            try {
                JsonNode root = jsonMapper.readTree(raw);
                if (!root.isArray())
                    throw new DeserializationException("Expected JSON array", raw);
                return root;
            } catch (Exception e) {
                throw new DeserializationException("Failed to deserialize JSON array", e);
            }
        };
    }

    /**
     * Returns a deserializer that parses any valid YAML string into a
     * {@link JsonNode} tree.
     *
     * @return a YAML deserializer
     */
    public static PayloadDeserializer<JsonNode> yaml() {
        return raw -> {
            try {
                return yamlMapper.readTree(raw);
            } catch (Exception e) {
                throw new DeserializationException("Failed to deserialize YAML", e);
            }
        };
    }

    /**
     * Returns a deserializer that maps a JSON string directly to an instance
     * of the given class using snake_case field mapping.
     *
     * <p>Useful in pipeline stages where the target type is known upfront.
     *
     * @param type the target class to deserialize into
     * @return a typed JSON deserializer
     */
    public static <T> PayloadDeserializer<T> jsonAs(Class<T> type) {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return raw -> {
            try {
                return mapper.readValue(raw, type);
            } catch (Exception e) {
                throw new DeserializationException(
                        "Failed to deserialize JSON as " + type.getSimpleName(), e);
            }
        };
    }


    /**
     * Returns a deserializer that parses a YAML string and validates that its
     * root element is a YAML sequence.
     *
     * @return a YAML sequence deserializer
     * @throws DeserializationException if the root element is not a YAML sequence
     */
    public static PayloadDeserializer<JsonNode> yamlArray() {
        return raw -> {
            try {
                JsonNode root = yamlMapper.readTree(raw);
                if (!root.isArray())
                    throw new DeserializationException("Expected YAML array", raw);
                return root;
            } catch (Exception e) {
                throw new DeserializationException("Failed to deserialize YAML array", e);
            }
        };
    }

    /**
     * Returns a deserializer that splits a raw string into trimmed, non-empty
     * lines and returns them as a {@code List<String>}.
     *
     * @return a text-lines deserializer
     */
    public static PayloadDeserializer<List<String>> textLines() {
        return raw -> {
            try {
                return Stream.of(raw.split("\n"))
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .toList();
            } catch (Exception e) {
                throw new DeserializationException("Failed to deserialize text lines", e);
            }
        };
    }

    /**
     * Returns a deserializer that parses a CSV string (with a header row) into
     * a list of {@code Map<String, String>} records, where each map is keyed by
     * the corresponding column header.
     *
     * <p>
     * Values and headers are trimmed. Columns with no matching value in a
     * row are simply omitted from the map.
     * </p>
     *
     * @return a CSV deserializer
     */
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
                throw new DeserializationException("Failed to deserialize CSV", e);
            }
        };
    }
}
