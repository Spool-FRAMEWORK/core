package software.spool.core.port;

import software.spool.core.exception.DeserializationException;

public interface PayloadDeserializer<R, T> {
    /**
     * Converts the given raw value into a typed intermediate object.
     *
     * @param payload the raw value to deserialize; may be {@code null} if the
     *               source legitimately produces empty responses
     * @return the deserialized representation; must not be {@code null}
     * @throws DeserializationException if the value cannot be parsed or converted
     */
    T deserialize(R payload) throws DeserializationException;
}
