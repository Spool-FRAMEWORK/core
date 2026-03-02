package software.spool.core.exception;

public class SerializationException extends SpoolException {

    public SerializationException(String failedPayload, Throwable cause) {
        super("Serialization failed for: " + failedPayload, cause);
    }

    public SerializationException(String message, String failedPayload) {
        super("Serialization failed for: " + failedPayload + ". " + message);
    }
}
