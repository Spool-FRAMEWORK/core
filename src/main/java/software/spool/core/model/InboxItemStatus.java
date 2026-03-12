package software.spool.core.model;

/**
 * Lifecycle statuses that an inbox item transitions through.
 *
 * <p>
 * The typical flow is:
 * {@code UNPUBLISHED → PUBLISHING → PUBLISHED → INGESTED → PERSISTED}.
 * Validation steps may include {@code VALIDATING → VALIDATED}.
 * </p>
 */
public enum InboxItemStatus {
    UNPUBLISHED,
    PUBLISHING,
    PUBLISHED,
    VALIDATING,
    VALIDATED,
    INGESTED,
    PERSISTED
}
