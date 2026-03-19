package software.spool.core.model;

import software.spool.core.infrastructure.adapter.PayloadDeserializerFactory;

import java.util.ArrayList;
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

        public PartitionKey from(final String payload) throws IllegalArgumentException {
            return new PartitionKey(validateAndBuild(payload));
        }

        private String validateAndBuild(String payload) throws IllegalArgumentException {
            String base = schema.value();
            List<String> attributes = new ArrayList<>();
            Map<String, Object> entries = PayloadDeserializerFactory.jsonObject().deserialize(payload);
            schema.attributes().forEach(a -> {
                if (!entries.containsKey(a))
                    throw new IllegalArgumentException("PartitionKeySchema does not match with payload");
                attributes.add(a + "=" + entries.get(a));
            });
            return base + "::" + String.join("::", attributes);
        }
    }
}
