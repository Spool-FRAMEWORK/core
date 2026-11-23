package software.spool.core.model.vo;

import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.exception.PartitionKeyException;

public class AutomaticBuilder {
    private final PartitionKeySchema schema;

    AutomaticBuilder(final PartitionKeySchema schema) {
        this.schema = schema;
    }

    public PartitionKey from(final byte[] payload) throws PartitionKeyException {
        return new PartitionKey(validateAndBuild(payload));
    }

    private String validateAndBuild(byte[] payload) {
        RecordFields fields = RecordFields.of(PayloadDeserializerFactory.json().asMap().deserialize(payload));

        KeyEntries keyEntries = PartitionKeyBuilderUtils.dateEntries().with("source", schema.sourceId());
        if (schema.eventType() != Void.class) {
            keyEntries.with("eventType", schema.eventType().getSimpleName());
        }
        for (String attribute : schema.attributes()) {
            Object value = fields.find(attribute).orElseThrow(() -> new PartitionKeyException(
                    new String(payload),
                    schema,
                    "Missing or null value for attribute path: " + attribute
            ));
            keyEntries.with(attribute, value);
        }
        return keyEntries.format();
    }
}
