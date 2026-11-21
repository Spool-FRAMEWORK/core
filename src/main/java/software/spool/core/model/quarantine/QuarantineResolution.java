package software.spool.core.model.quarantine;

import software.spool.core.model.vo.IdempotencyKey;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * How one stay in quarantine ended, and who ended it, or that it has not ended yet. A stay resolved before the
 * resolver was kept has an {@link Resolver.Unknown} one.
 */
public sealed interface QuarantineResolution {

    QuarantineStatus status();

    Optional<Instant> resolvedAt();

    static QuarantineResolution pending() {
        return new Pending();
    }

    static QuarantineResolution republished(Instant resolvedAt, Resolver resolver) {
        return new Republished(resolvedAt, resolver);
    }

    static QuarantineResolution corrected(Instant resolvedAt, IdempotencyKey correctedTo, Resolver resolver) {
        return new Corrected(resolvedAt, correctedTo, resolver);
    }

    static QuarantineResolution bypassed(Instant resolvedAt, String reason, Resolver resolver) {
        return new Bypassed(resolvedAt, reason, resolver);
    }

    static QuarantineResolution purged(Instant resolvedAt, Resolver resolver) {
        return new Purged(resolvedAt, resolver);
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
    record Republished(Instant at, Resolver by) implements QuarantineResolution {
        public Republished {
            Objects.requireNonNull(at, "at is required");
            by = Objects.requireNonNullElseGet(by, Resolver::unknown);
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
    record Corrected(Instant at, IdempotencyKey correctedTo, Resolver by) implements QuarantineResolution {
        public Corrected {
            Objects.requireNonNull(at, "at is required");
            Objects.requireNonNull(correctedTo, "correctedTo is required");
            by = Objects.requireNonNullElseGet(by, Resolver::unknown);
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
    record Bypassed(Instant at, String reason, Resolver by) implements QuarantineResolution {
        public Bypassed {
            Objects.requireNonNull(at, "at is required");
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("A bypass requires a reason; the reason given is empty");
            }
            by = Objects.requireNonNullElseGet(by, Resolver::unknown);
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
    record Purged(Instant at, Resolver by) implements QuarantineResolution {
        public Purged {
            Objects.requireNonNull(at, "at is required");
            by = Objects.requireNonNullElseGet(by, Resolver::unknown);
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
