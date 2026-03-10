package software.spool.core.port;

import software.spool.core.exception.SerializationException;

public interface RecordSerializer<T> {
    /**
     * Converts the given record into a {@code String} payload.
     *
     * @param record   the record to serialize; must not be {@code null}
     * @return the serialized string representation; must not be {@code null}
     * @throws SerializationException if the record cannot be serialized
     */
    String serialize(T record) throws SerializationException;
}
