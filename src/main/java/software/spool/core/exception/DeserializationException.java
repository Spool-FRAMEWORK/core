package software.spool.core.exception;

public class DeserializationException extends SpoolException {
    public DeserializationException(String rawPayload, Throwable cause) {
        super("Deserialization failed for: " + rawPayload, cause);
    }

    public DeserializationException(String message, String rawPayload) {
        super("Deserialization failed for: " + rawPayload + ". " + message);
    }
}