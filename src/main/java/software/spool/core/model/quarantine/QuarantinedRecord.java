package software.spool.core.model.quarantine;

import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * What is kept of an envelope in quarantine: the whole envelope, where it stayed, and every time it came into
 * quarantine with how that time ended. The last entry is the current one.
 */
public record QuarantinedRecord(
        Envelope envelope,
        QuarantineLocation location,
        List<QuarantineEntry> history) {

    public QuarantinedRecord {
        Objects.requireNonNull(envelope, "envelope is required");
        Objects.requireNonNull(location, "location is required");
        Objects.requireNonNull(history, "history is required");
        if (history.isEmpty()) {
            throw new IllegalArgumentException("Quarantine record of " + envelope.idempotencyKey().value()
                    + " needs at least one entry in its history");
        }
        history = List.copyOf(history);
    }

    public static QuarantinedRecord pending(Envelope envelope, QuarantineLocation location, QuarantineReason reason,
                                            List<String> violations, Instant quarantinedAt) {
        return new QuarantinedRecord(envelope, location,
                List.of(new QuarantineEntry(reason, violations, quarantinedAt, QuarantineResolution.pending())));
    }

    public IdempotencyKey idempotencyKey() {
        return envelope.idempotencyKey();
    }

    public QuarantineReason reason() {
        return current().reason();
    }

    public List<String> violations() {
        return current().violations();
    }

    public Instant quarantinedAt() {
        return current().quarantinedAt();
    }

    public QuarantineResolution resolution() {
        return current().resolution();
    }

    public QuarantineStatus status() {
        return resolution().status();
    }

    /**
     * Ends the current stay in quarantine.
     *
     * @throws IllegalStateException if the record is not pending
     */
    public QuarantinedRecord resolve(QuarantineResolution newResolution) {
        if (status() != QuarantineStatus.PENDING) {
            throw new IllegalStateException("Quarantine record of " + idempotencyKey().value()
                    + " cannot be resolved: it is already " + status());
        }
        List<QuarantineEntry> resolvedHistory = new ArrayList<>(history.subList(0, history.size() - 1));
        resolvedHistory.add(current().resolvedAs(newResolution));
        return new QuarantinedRecord(envelope, location, resolvedHistory);
    }

    /**
     * Brings a resolved record back into quarantine with a new entry. A record that is still pending stays as it
     * is, because the same rejection can arrive more than once.
     */
    public QuarantinedRecord reenter(QuarantineReason reason, List<String> violations, Instant quarantinedAt) {
        if (status() == QuarantineStatus.PENDING) return this;
        List<QuarantineEntry> reopenedHistory = new ArrayList<>(history);
        reopenedHistory.add(new QuarantineEntry(reason, violations, quarantinedAt, QuarantineResolution.pending()));
        return new QuarantinedRecord(envelope, location, reopenedHistory);
    }

    private QuarantineEntry current() {
        return history.get(history.size() - 1);
    }
}
