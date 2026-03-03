package software.spool.core.exception;

public class SerializationException extends SpoolException {

    private final String paylaod;

    public SerializationException(String payload, Throwable cause) {
        super("Serialization failed for: " + payload, cause);
        this.paylaod = payload;
    }

    public SerializationException(String message, String payload) {
        super("Serialization failed for: " + payload + ". " + message);
        this.paylaod = payload;
    }

    public String getPaylaod() {
        return paylaod;
    }
}
