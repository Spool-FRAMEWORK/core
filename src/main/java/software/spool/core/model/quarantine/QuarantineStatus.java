package software.spool.core.model.quarantine;

/**
 * Where a quarantine record stands: waiting for someone to resolve it, or how it was resolved.
 */
public enum QuarantineStatus {
    PENDING,
    REPUBLISHED,
    CORRECTED,
    BYPASSED,
    PURGED
}
