package software.spool.core.exception;

import software.spool.core.model.vo.PartitionKeySchema;
import software.spool.core.port.serde.RecordSerializer;

/**
 * Thrown when a {@link RecordSerializer} fails
 * to serialize a record into a string payload.
 */
public class PartitionKeyException extends SpoolException {
    private final String payload;
    private final PartitionKeySchema schema;

    public PartitionKeyException(String payload, PartitionKeySchema schema, Throwable cause) {
        super(String.format("PartitionKey failed for schema [%s]: %s", schema, payload), cause);
        this.payload = payload;
        this.schema = schema;
    }

    public PartitionKeyException(String payload, PartitionKeySchema schema, String message) {
        super(String.format("PartitionKey failed for schema [%s]; %s: %s", schema, payload, message));
        this.payload = payload;
        this.schema = schema;
    }

    public String getPayload() {
        return payload;
    }
    public PartitionKeySchema getPartitionKeySchema() { return schema; }
}
