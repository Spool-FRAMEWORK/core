package software.spool.core.exception;

public class SerializationException extends SpoolException {
    private final String payload;

    public SerializationException(String payload, Throwable cause) {
        super("Serialization failed for: " + payload, cause);
        this.payload = payload;
    }

    public SerializationException(String payload, String message) {
        super("Serialization failed for: " + payload + ". " + message);
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
