package software.spool.core.model.vo;

import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.exception.DeserializationException;

import java.util.Map;
import java.util.Optional;

/**
 * The fields of a record read as a JSON object, found by a path written with dots ({@code meta.id}), as a
 * {@link PartitionKeySchema} or an {@link IdempotencyKeySchema} names them.
 */
public final class RecordFields {
    private static final String PATH_SEPARATOR = "\\.";

    private final Map<String, Object> fields;

    private RecordFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public static RecordFields of(Map<String, Object> fields) {
        return new RecordFields(fields);
    }

    /** The fields of a record, or none if the record is not a JSON object, as a PDF or a line of text. */
    public static Optional<RecordFields> of(byte[] record) {
        try {
            return Optional.of(new RecordFields(PayloadDeserializerFactory.json().asMap().deserialize(record)));
        } catch (DeserializationException e) {
            return Optional.empty();
        }
    }

    /** The value at the path, or none if a part of the path is missing, is not an object or holds null. */
    public Optional<Object> find(String path) {
        Object current = fields;
        for (String part : path.split(PATH_SEPARATOR)) {
            if (!(current instanceof Map<?, ?> object) || !object.containsKey(part)) {
                return Optional.empty();
            }
            current = object.get(part);
        }
        return Optional.ofNullable(current);
    }
}
