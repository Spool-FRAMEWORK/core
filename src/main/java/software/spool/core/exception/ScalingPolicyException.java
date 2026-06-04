package software.spool.core.exception;

public class ScalingPolicyException extends SpoolException {
    public ScalingPolicyException(String message) {
        super("Scaling policy resolution failed: " + message);
    }
}
