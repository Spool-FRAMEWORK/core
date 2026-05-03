package software.spool.core.model.vo;

import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.exception.PartitionKeyException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record PartitionKey(String value) {
    public PartitionKey(final String value) {
        Objects.requireNonNull(value);
        this.value = value;
    }

    public static Builder of(PartitionKeySchema schema) {
        return new Builder(schema);
    }

    public static class Builder {
        private final PartitionKeySchema schema;

        public Builder(final PartitionKeySchema schema) {
            this.schema = schema;
        }

        public PartitionKey from(final String payload) throws PartitionKeyException {
            return new PartitionKey(validateAndBuild(payload));
        }

        private String validateAndBuild(String payload) {
            Map<String, Object> entries = PayloadDeserializerFactory.json().asMap().deserialize(payload);

            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            String datePrefix = "year=" + now.getYear()
                    + "::month=" + String.format("%02d", now.getMonthValue())
                    + "::day=" + String.format("%02d", now.getDayOfMonth())
                    + "::hour=" + String.format("%02d", now.getHour());

            List<String> resolved = schema.attributes().stream()
                    .map(attribute -> {
                        Object value = getNestedValue(entries, attribute, payload);
                        if (value == null) {
                            throw new PartitionKeyException(
                                    payload,
                                    schema,
                                    "Missing or null value for attribute path: " + attribute
                            );
                        }
                        return attribute + "=" + escapeValue(value);
                    })
                    .toList();

            String base = datePrefix + "::source=" + schema.sourceId();
            if (schema.eventType() != Void.class) {
                base += "::" + schema.eventType().getSimpleName();
            }

            return resolved.isEmpty() ? base : base + "::" + String.join("::", resolved);
        }

        @SuppressWarnings("unchecked")
        private Object getNestedValue(Map<String, Object> entries, String path, String payload) {
            String[] parts = path.split("\\.");
            Object current = entries;

            for (String part : parts) {
                if (!(current instanceof Map<?, ?> currentMap)) {
                    throw new PartitionKeyException(
                            payload,
                            schema,
                            "Invalid attribute path: " + path + ". Segment '" + part + "' is not an object"
                    );
                }

                if (!currentMap.containsKey(part)) {
                    throw new PartitionKeyException(
                            payload,
                            schema,
                            "Missing attribute path: " + path
                    );
                }

                current = ((Map<String, Object>) currentMap).get(part);
            }

            return current;
        }

        private String escapeValue(Object value) {
            return String.valueOf(value)
                    .replace("\\", "\\\\")
                    .replace("::", "\\:\\:")
                    .replace("=", "\\=");
        }
    }
}