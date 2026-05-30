package software.spool.core.model.vo;

import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.exception.PartitionKeyException;

import java.util.List;
import java.util.Map;

public class AutomaticBuilder {
    private final PartitionKeySchema schema;

    AutomaticBuilder(final PartitionKeySchema schema) {
        this.schema = schema;
    }

    public PartitionKey from(final byte[] payload) throws PartitionKeyException {
        return new PartitionKey(validateAndBuild(payload));
    }

    private String validateAndBuild(byte[] payload) {
        Map<String, Object> entries = PayloadDeserializerFactory.json().asMap().deserialize(payload);

        List<String> resolved = schema.attributes().stream()
                .map(attribute -> {
                    Object value = getNestedValue(entries, attribute, payload);
                    if (value == null) {
                        throw new PartitionKeyException(
                                new String(payload),
                                schema,
                                "Missing or null value for attribute path: " + attribute
                        );
                    }
                    return attribute + "=" + PartitionKeyBuilderUtils.escapeValue(value);
                })
                .toList();

        String base = PartitionKeyBuilderUtils.datePrefix() + "::source=" + schema.sourceId();
        if (schema.eventType() != Void.class) {
            base += "::eventType=" + schema.eventType().getSimpleName();
        }

        return resolved.isEmpty() ? base : base + "::" + String.join("::", resolved);
    }

    @SuppressWarnings("unchecked")
    private Object getNestedValue(Map<String, Object> entries, String path, byte[] payload) {
        String[] parts = path.split("\\.");
        Object current = entries;

        for (String part : parts) {
            if (!(current instanceof Map<?, ?> currentMap)) {
                throw new PartitionKeyException(
                        new String(payload),
                        schema,
                        "Invalid attribute path: " + path + ". Segment '" + part + "' is not an object"
                );
            }
            if (!currentMap.containsKey(part)) {
                throw new PartitionKeyException(
                        new String(payload),
                        schema,
                        "Missing attribute path: " + path
                );
            }
            current = ((Map<String, Object>) currentMap).get(part);
        }

        return current;
    }
}