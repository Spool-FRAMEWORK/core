package software.spool.core.model.vo;

import software.spool.core.adapter.jackson.PayloadDeserializerFactory;

import java.util.*;

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

        public PartitionKey from(final String payload) throws IllegalArgumentException {
            return new PartitionKey(validateAndBuild(payload));
        }

        private String validateAndBuild(String payload) {
            Map<String, Object> entries = PayloadDeserializerFactory.jsonObject().deserialize(payload);
            List<String> resolved = schema.attributes().stream()
                    .map(a -> {
                        if (!entries.containsKey(a)) {
                            throw new IllegalArgumentException("PartitionKeySchema does not match with payload");
                        }
                        return a + "=" + entries.get(a);
                    })
                    .toList();
            String base = schema.sourceId();
            if (schema.eventType() != Void.class)
                base += "::" + schema.eventType();
            return resolved.isEmpty() ? base : base + "::" + String.join("::", resolved);
        }
    }
}
