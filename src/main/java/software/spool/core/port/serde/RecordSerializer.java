package software.spool.core.port.serde;

import software.spool.core.adapter.jackson.RecordSerializerFactory;
import software.spool.core.exception.SerializationException;

/**
 * Output port for converting typed records into string payloads.
 *
 * <p>
 * Ready-made implementations are available via
 * {@link RecordSerializerFactory}.
 * </p>
 *
 * @param <R> the input record type
 */
public interface RecordSerializer<R> {
    /**
     * Converts the given record into a {@code String} payload.
     *
     * @param record the record to serialize; must not be {@code null}
     * @return the serialized string representation; must not be {@code null}
     * @throws SerializationException if the record cannot be serialized
     */
    String serialize(R record) throws SerializationException;
}
