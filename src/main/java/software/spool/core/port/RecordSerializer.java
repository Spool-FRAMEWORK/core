package software.spool.core.port;

import software.spool.core.exception.SerializationException;

/**
 * Output port for converting typed records into string payloads.
 *
 * <p>
 * Ready-made implementations are available via
 * {@link software.spool.core.infrastructure.adapter.RecordSerializerFactory}.
 * </p>
 *
 * @param <T> the input record type
 */
public interface RecordSerializer<T> {
    /**
     * Converts the given record into a {@code String} payload.
     *
     * @param record the record to serialize; must not be {@code null}
     * @return the serialized string representation; must not be {@code null}
     * @throws SerializationException if the record cannot be serialized
     */
    String serialize(T record) throws SerializationException;
}
