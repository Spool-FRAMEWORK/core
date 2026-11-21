package software.spool.core.utils.quarantine;

import org.junit.jupiter.api.Test;
import software.spool.core.adapter.memory.InMemoryQuarantineRepository;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.quarantine.QuarantineLocation;
import software.spool.core.model.quarantine.QuarantineReason;
import software.spool.core.model.quarantine.QuarantineResolution;
import software.spool.core.model.quarantine.Resolver;
import software.spool.core.model.quarantine.QuarantineStatus;
import software.spool.core.model.quarantine.QuarantinedRecord;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.EventMetadata;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.utils.media.MediaTypes;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuarantineRecorderTest {

    private static final IdempotencyKey KEY = IdempotencyKey.of("k1");
    private static final Instant FIRST_TIME = Instant.parse("2026-11-01T10:00:00Z");
    private static final Instant SECOND_TIME = Instant.parse("2026-11-02T10:00:00Z");

    private final InMemoryQuarantineRepository repository = new InMemoryQuarantineRepository();
    private final QuarantineRecorder recorder = new QuarantineRecorder(repository);

    @Test
    void record_keyWithoutRecord_savesAPendingRecordWhereTheEnvelopeStayed() {
        PartitionKey partition = PartitionKey.ofEntriesWithoutDate().with("source", "boe").build();

        recorder.record(envelope(), QuarantineLocation.bronze(partition), QuarantineReason.VALIDATION_FAILED,
                List.of("price must be positive"), FIRST_TIME);

        QuarantinedRecord saved = repository.find(KEY).orElseThrow();
        assertThat(saved.status()).isEqualTo(QuarantineStatus.PENDING);
        assertThat(saved.location()).isEqualTo(QuarantineLocation.bronze(partition));
        assertThat(saved.violations()).containsExactly("price must be positive");
    }

    @Test
    void record_keyWithAResolvedRecord_reopensIt() {
        repository.save(QuarantinedRecord.pending(envelope(), QuarantineLocation.inbox(), QuarantineReason.VALIDATION_FAILED,
                List.of(), FIRST_TIME).resolve(QuarantineResolution.republished(FIRST_TIME, Resolver.identified("ana", "people"))));

        recorder.record(envelope(), QuarantineLocation.inbox(), QuarantineReason.RETRIES_EXHAUSTED, List.of(), SECOND_TIME);

        QuarantinedRecord reopened = repository.find(KEY).orElseThrow();
        assertThat(reopened.status()).isEqualTo(QuarantineStatus.PENDING);
        assertThat(reopened.history()).hasSize(2);
    }

    @Test
    void record_keyWithAPendingRecord_keepsItsHistory() {
        recorder.record(envelope(), QuarantineLocation.inbox(), QuarantineReason.VALIDATION_FAILED, List.of(), FIRST_TIME);

        recorder.record(envelope(), QuarantineLocation.inbox(), QuarantineReason.VALIDATION_FAILED, List.of(), SECOND_TIME);

        assertThat(repository.find(KEY).orElseThrow().history()).hasSize(1);
    }

    private static Envelope envelope() {
        return new Envelope(KEY, new EventMetadata(), MediaTypes.JSON, "{}".getBytes(StandardCharsets.UTF_8),
                EnvelopeStatus.CAPTURED, 0, FIRST_TIME, FIRST_TIME);
    }
}
