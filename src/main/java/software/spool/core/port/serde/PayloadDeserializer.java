package software.spool.core.port.serde;

import software.spool.core.adapter.jackson.PayloadDeserializerFactory;
import software.spool.core.exception.DeserializationException;

/**
 * Input port for converting raw string payloads into typed objects.
 *
 * <p>
 * Ready-made implementations are available via
 * {@link PayloadDeserializerFactory}.
 * </p>
 *
 * @param <P> the deserialized output type
 */
public interface PayloadDeserializer<P> {
    /**
     * Converts the given raw value into a typed intermediate object.
     *
     * @param payload the raw value to deserialize; may be {@code null} if the
     *                source legitimately produces empty responses
     * @return the deserialized representation; must not be {@code null}
     * @throws DeserializationException if the value cannot be parsed or converted
     */
    P deserialize(String payload) throws DeserializationException;
}
