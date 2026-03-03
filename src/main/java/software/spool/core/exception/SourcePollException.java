package software.spool.core.exception;

public class SourcePollException extends SpoolException {
    private final String sourceId;

    public SourcePollException(String sourceId, String message) {
        super("Error while polling " + sourceId + ": " + message);
        this.sourceId = sourceId;
    }
    public SourcePollException(String sourceId, String message, Throwable cause) {
        super("Error while polling " + sourceId + ": " + message, cause);
        this.sourceId = sourceId;
    }

    public String getSourceId() {
        return sourceId;
    }
}
