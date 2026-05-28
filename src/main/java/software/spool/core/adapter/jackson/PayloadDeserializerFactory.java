package software.spool.core.adapter.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import software.spool.core.exception.DeserializationException;
import software.spool.core.port.serde.PayloadDeserializer;
import software.spool.core.spi.SerdeRegistry;
import software.spool.core.spi.factory.StructuredPayloadDeserializerBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class PayloadDeserializerFactory {
    public static <P> PayloadDeserializer<P> noOp() {
        return payload -> (P) payload;
    }

    public static StructuredPayloadDeserializerBuilder json() {
        return SerdeRegistry.structured("JSON").builder();
    }

    public static StructuredPayloadDeserializerBuilder yaml() {
        return SerdeRegistry.structured("YAML").builder();
    }

    public static PayloadDeserializer<List<String>> textLines() {
        return raw -> {
            try {
                return Stream.of(new String(raw).split("\n"))
                        .map(String::trim)
                        .filter(line -> !line.isEmpty())
                        .toList();
            } catch (Exception e) {
                throw new DeserializationException(new String(raw), e.getMessage());
            }
        };
    }

    public static PayloadDeserializer<List<Map<String, String>>> csv() {
        return raw -> {
            try {
                String rawString = new String(raw);
                String[] headers = rawString.split("\n")[0].split(",");
                return Stream.of(rawString.split("\n"))
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
                throw new DeserializationException(new String(raw), e.getMessage());
            }
        };
    }

    private PayloadDeserializerFactory() {}
}