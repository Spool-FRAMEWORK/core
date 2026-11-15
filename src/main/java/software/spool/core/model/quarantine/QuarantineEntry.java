package software.spool.core.model.quarantine;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * One stay of an envelope in quarantine: why and when it came in, and how it ended.
 */
public record QuarantineEntry(
        QuarantineReason reason,
        List<String> violations,
        Instant quarantinedAt,
        QuarantineResolution resolution) {

    public QuarantineEntry {
        Objects.requireNonNull(reason, "reason is required");
        Objects.requireNonNull(quarantinedAt, "quarantinedAt is required");
        Objects.requireNonNull(resolution, "resolution is required");
        violations = Objects.isNull(violations) ? List.of() : List.copyOf(violations);
    }

    QuarantineEntry resolvedAs(QuarantineResolution newResolution) {
        return new QuarantineEntry(reason, violations, quarantinedAt, newResolution);
    }
}
