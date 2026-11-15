package software.spool.core.utils.quarantine;

import software.spool.core.model.quarantine.QuarantineLocation;
import software.spool.core.model.quarantine.QuarantineReason;
import software.spool.core.model.quarantine.QuarantinedRecord;
import software.spool.core.model.vo.Envelope;
import software.spool.core.port.quarantine.QuarantineRepository;

import java.time.Instant;
import java.util.List;

/**
 * Records that an envelope came into quarantine: a key seen for the first time gets a new pending record, a resolved
 * one is reopened with a new entry in its history, and a pending one is left as it is.
 */
public class QuarantineRecorder {
    private final QuarantineRepository repository;

    public QuarantineRecorder(QuarantineRepository repository) {
        this.repository = repository;
    }

    public QuarantinedRecord record(Envelope envelope, QuarantineLocation location, QuarantineReason reason,
                                    List<String> violations, Instant quarantinedAt) {
        QuarantinedRecord recorded = repository.find(envelope.idempotencyKey())
                .map(existing -> existing.reenter(reason, violations, quarantinedAt))
                .orElseGet(() -> QuarantinedRecord.pending(envelope, location, reason, violations, quarantinedAt));
        repository.save(recorded);
        return recorded;
    }
}
