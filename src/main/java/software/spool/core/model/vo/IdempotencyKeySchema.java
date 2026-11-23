package software.spool.core.model.vo;

import software.spool.core.exception.IdempotencyKeyException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The fields of a record its {@link IdempotencyKey} is built from, as a {@link PartitionKeySchema} gives the attributes
 * of its partition. Two records of a source with the same values in those fields have the same key, whatever else they
 * hold. Without fields, the key comes from the whole record.
 *
 * @param fields             paths of the fields, written with dots ({@code meta.id})
 * @param whenFieldIsMissing what to do with a record that lacks one of them, or that is not a JSON object
 */
public record IdempotencyKeySchema(List<String> fields, WhenFieldIsMissing whenFieldIsMissing) {
    /** What happens to a record without a field of the schema. */
    public enum WhenFieldIsMissing {
        /** The record is rejected with an {@link IdempotencyKeyException}. */
        FAIL,
        /** The record is keyed by the whole record, as without a schema. */
        WHOLE_RECORD
    }

    public IdempotencyKeySchema {
        fields = List.copyOf(fields);
        Objects.requireNonNull(whenFieldIsMissing, "whenFieldIsMissing is required");
    }

    /** A schema that rejects a record without one of the fields. */
    public static IdempotencyKeySchema of(List<String> fields) {
        return new IdempotencyKeySchema(fields, WhenFieldIsMissing.FAIL);
    }

    /** The key of the whole record, as a source without a schema has. */
    public static IdempotencyKeySchema wholeRecord() {
        return new IdempotencyKeySchema(List.of(), WhenFieldIsMissing.WHOLE_RECORD);
    }

    /**
     * @throws IdempotencyKeyException if the record lacks a field of the schema and the schema says to fail
     */
    public IdempotencyKey keyOf(String sourceId, byte[] record) throws IdempotencyKeyException {
        if (fields.isEmpty()) return IdempotencyKey.of(sourceId, record);
        Optional<RecordFields> recordFields = RecordFields.of(record);
        KeyEntries values = new KeyEntries();
        for (String field : fields) {
            Optional<Object> value = recordFields.flatMap(found -> found.find(field));
            if (value.isEmpty()) return withoutField(sourceId, record, field);
            values.with(field, value.get());
        }
        return IdempotencyKey.of(sourceId, values.format());
    }

    private IdempotencyKey withoutField(String sourceId, byte[] record, String field) {
        if (whenFieldIsMissing == WhenFieldIsMissing.FAIL) throw new IdempotencyKeyException(sourceId, field);
        return IdempotencyKey.of(sourceId, record);
    }
}
