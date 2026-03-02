package software.spool.core.exception;

public class SourceSplitException extends SpoolException {
    public SourceSplitException(String rawInput) {
        super("Split failed for: " + rawInput);
    }

    public SourceSplitException(String message, String rawInput) {
        super("Split failed for: " + rawInput + ". " + message);
    }
}
