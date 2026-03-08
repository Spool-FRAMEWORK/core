package software.spool.core.model;

public enum InboxItemStatus {
    UNPUBLISHED,
    PUBLISHING,
    PUBLISHED,
    VALIDATING,
    VALIDATED,
    INGESTED,
    PERSISTED
}
