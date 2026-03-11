package software.spool.core.exception;

public class SplitException extends SpoolException {
    private final String payload;

    public SplitException(String payload) {
        super("Split failed for: " + payload);
        this.payload = payload;
    }

    public SplitException(String message, String payload) {
        super("Split failed for: " + payload + ". " + message);
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
