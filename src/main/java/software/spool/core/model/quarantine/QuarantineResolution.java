package software.spool.core.model.quarantine;

import software.spool.core.model.vo.IdempotencyKey;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * How one stay in quarantine ended, or that it has not ended yet.
 */
public sealed interface QuarantineResolution {

    QuarantineStatus status();

    Optional<Instant> resolvedAt();

    static QuarantineResolution pending() {
        return new Pending();
    }

    static QuarantineResolution republished(Instant resolvedAt) {
        return new Republished(resolvedAt);
    }

    static QuarantineResolution corrected(Instant resolvedAt, IdempotencyKey correctedTo) {
        return new Corrected(resolvedAt, correctedTo);
    }

    static QuarantineResolution bypassed(Instant resolvedAt, String reason) {
        return new Bypassed(resolvedAt, reason);
    }

    static QuarantineResolution purged(Instant resolvedAt) {
        return new Purged(resolvedAt);
    }

    record Pending() implements QuarantineResolution {
        @Override
        public QuarantineStatus status() {
            return QuarantineStatus.PENDING;
        }

        @Override
        public Optional<Instant> resolvedAt() {
            return Optional.empty();
        }
    }

    /**
     * Sent back as it was.
     */
    record Republished(Instant at) implements QuarantineResolution {
        public Republished {
            Objects.requireNonNull(at, "at is required");
        }

        @Override
        public QuarantineStatus status() {
            return QuarantineStatus.REPUBLISHED;
        }

        @Override
        public Optional<Instant> resolvedAt() {
            return Optional.of(at);
        }
    }

    /**
     * Replaced by an envelope with a corrected payload, under the key {@code correctedTo}.
     */
    record Corrected(Instant at, IdempotencyKey correctedTo) implements QuarantineResolution {
        public Corrected {
            Objects.requireNonNull(at, "at is required");
            Objects.requireNonNull(correctedTo, "correctedTo is required");
        }

        @Override
        public QuarantineStatus status() {
            return QuarantineStatus.CORRECTED;
        }

        @Override
        public Optional<Instant> resolvedAt() {
            return Optional.of(at);
        }
    }

    /**
     * Sent back as it was, marked so that validation lets it through, because a person judged it valid for
     * {@code reason}.
     */
    record Bypassed(Instant at, String reason) implements QuarantineResolution {
        public Bypassed {
            Objects.requireNonNull(at, "at is required");
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("A bypass requires a reason; the reason given is empty");
            }
        }

        @Override
        public QuarantineStatus status() {
            return QuarantineStatus.BYPASSED;
        }

        @Override
        public Optional<Instant> resolvedAt() {
            return Optional.of(at);
        }
    }

    /**
     * Discarded for good.
     */
    record Purged(Instant at) implements QuarantineResolution {
        public Purged {
            Objects.requireNonNull(at, "at is required");
        }

        @Override
        public QuarantineStatus status() {
            return QuarantineStatus.PURGED;
        }

        @Override
        public Optional<Instant> resolvedAt() {
            return Optional.of(at);
        }
    }
}
