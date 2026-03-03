package software.spool.core.exception;

public class SourceSplitException extends SpoolException {
    private final String payload;

    public SourceSplitException(String payload) {
        super("Split failed for: " + payload);
        this.payload = payload;
    }

    public SourceSplitException(String message, String payload) {
        super("Split failed for: " + payload + ". " + message);
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
