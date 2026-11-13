package software.spool.core.model.quarantine;

import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * What is kept of an envelope in quarantine: the whole envelope as it was in the inbox, why it was quarantined,
 * the violations that caused it, if any, and when.
 */
public record QuarantinedRecord(
        Envelope envelope,
        QuarantineReason reason,
        List<String> violations,
        Instant quarantinedAt) {

    public QuarantinedRecord {
        Objects.requireNonNull(envelope, "envelope is required");
        Objects.requireNonNull(reason, "reason is required");
        Objects.requireNonNull(quarantinedAt, "quarantinedAt is required");
        violations = Objects.isNull(violations) ? List.of() : List.copyOf(violations);
    }

    public IdempotencyKey idempotencyKey() {
        return envelope.idempotencyKey();
    }
}
