package software.spool.core.exception;

public class DeserializationException extends SpoolException {
    private final String payload;
    
    public DeserializationException(String payload, Throwable cause) {
        super("Deserialization failed for: " + payload, cause);
        this.payload = payload;
    }

    public DeserializationException(String message, String payload) {
        super("Deserialization failed for: " + payload + ". " + message);
        this.payload = payload;
    }
    
    public String getPayload() {
        return payload;
    }
}