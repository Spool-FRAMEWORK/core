package software.spool.core.exception;

import software.spool.core.port.serde.PayloadDeserializer;

/**
 * Thrown when a {@link PayloadDeserializer} fails
 * to parse a raw payload.
 */
public class DeserializationException extends SpoolException {
    private final String payload;

    public DeserializationException(String payload, Throwable cause) {
        super("Deserialization failed for: " + payload, cause);
        this.payload = payload;
    }

    public DeserializationException(String payload, String message) {
        super("Deserialization failed for: " + payload + ". " + message);
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}