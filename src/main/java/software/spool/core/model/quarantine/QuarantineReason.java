package software.spool.core.model.quarantine;

/**
 * Why an envelope ended up in quarantine.
 */
public enum QuarantineReason {
    VALIDATION_FAILED,
    RETRIES_EXHAUSTED
}
