package software.spool.core.exception;

/**
 * Thrown when opening or connecting to a source fails.
 */
public class SourceOpenException extends SpoolException {
    private final String sourceId;

    public SourceOpenException(String sourceId, String message) {
        super("Error while fetching from source (" + sourceId + ")" + ": " + message);
        this.sourceId = sourceId;
    }

    public SourceOpenException(String sourceId, String message, Throwable cause) {
        super("Error while fetching from source (" + sourceId + ")" + ": " + message, cause);
        this.sourceId = sourceId;
    }

    public String getSourceId() {
        return sourceId;
    }
}
