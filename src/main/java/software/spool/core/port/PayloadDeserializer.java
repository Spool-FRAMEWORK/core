package software.spool.core.port;

import software.spool.core.exception.DeserializationException;

/**
 * Input port for converting raw string payloads into typed objects.
 *
 * <p>
 * Ready-made implementations are available via
 * {@link software.spool.core.infrastructure.adapter.PayloadDeserializerFactory}.
 * </p>
 *
 * @param <T> the deserialized output type
 */
public interface PayloadDeserializer<T> {
    /**
     * Converts the given raw value into a typed intermediate object.
     *
     * @param payload the raw value to deserialize; may be {@code null} if the
     *                source legitimately produces empty responses
     * @return the deserialized representation; must not be {@code null}
     * @throws DeserializationException if the value cannot be parsed or converted
     */
    T deserialize(String payload) throws DeserializationException;
}
