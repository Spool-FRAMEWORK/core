package software.spool.core.model.vo;

import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.exception.PartitionKeyException;

import java.time.LocalDate;
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

            LocalDate today = LocalDate.now(ZoneOffset.UTC);
            String datePrefix = "year=" + today.getYear()
                    + "::month=" + String.format("%02d", today.getMonthValue())
                    + "::day=" + String.format("%02d", today.getDayOfMonth());

            List<String> resolved = schema.attributes().stream()
                    .map(a -> {
                        if (!entries.containsKey(a))
                            throw new PartitionKeyException(payload, schema, "PartitionKeySchema does not match with payload");
                        return a + "=" + entries.get(a);
                    })
                    .toList();

            String base = datePrefix + "::" + schema.sourceId();
            if (schema.eventType() != Void.class) {
                base += "::" + schema.eventType();
            }

            return resolved.isEmpty() ? base : base + "::" + String.join("::", resolved);
        }
    }
}