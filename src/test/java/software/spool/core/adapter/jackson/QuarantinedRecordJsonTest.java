package software.spool.core.adapter.jackson;

import org.junit.jupiter.api.Test;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.quarantine.QuarantineLocation;
import software.spool.core.model.quarantine.QuarantineReason;
import software.spool.core.model.quarantine.QuarantineResolution;
import software.spool.core.model.quarantine.QuarantinedRecord;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.EventMetadata;
import software.spool.core.model.vo.EventMetadataKey;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.utils.media.MediaTypes;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuarantinedRecordJsonTest {

    private static final Instant FIRST_TIME = Instant.parse("2026-11-01T10:00:00Z");
    private static final Instant SECOND_TIME = Instant.parse("2026-11-02T10:00:00Z");

    @Test
    void roundTrip_pendingRecordInTheInbox_keepsEverything() {
        QuarantinedRecord original = QuarantinedRecord.pending(envelope(), QuarantineLocation.inbox(),
                QuarantineReason.VALIDATION_FAILED, List.of("price must be positive"), FIRST_TIME);

        QuarantinedRecord read = roundTrip(original);

        assertThat(read.location()).isEqualTo(QuarantineLocation.inbox());
        assertThat(read.history()).isEqualTo(original.history());
        assertThat(read.envelope().payload()).isEqualTo(original.envelope().payload());
        assertThat(read.envelope().metadata().get(EventMetadataKey.SOURCE)).isEqualTo("boe");
    }

    @Test
    void roundTrip_correctedRecordThatStayedInBronze_keepsItsPartitionAndCorrectedKey() {
        PartitionKey partition = PartitionKey.ofEntriesWithoutDate().with("source", "boe").build();
        QuarantinedRecord original = QuarantinedRecord.pending(envelope(), QuarantineLocation.bronze(partition),
                        QuarantineReason.VALIDATION_FAILED, List.of(), FIRST_TIME)
                .resolve(QuarantineResolution.corrected(SECOND_TIME, IdempotencyKey.of("corrected-key")))
                .reenter(QuarantineReason.VALIDATION_FAILED, List.of("again"), SECOND_TIME);

        QuarantinedRecord read = roundTrip(original);

        assertThat(read.location()).isEqualTo(QuarantineLocation.bronze(partition));
        assertThat(read.history()).isEqualTo(original.history());
    }

    private static QuarantinedRecord roundTrip(QuarantinedRecord quarantinedRecord) {
        byte[] json = RecordSerializerFactory.<QuarantinedRecord>record().serialize(quarantinedRecord);
        return PayloadDeserializerFactory.json().as(QuarantinedRecord.class).deserialize(json);
    }

    private static Envelope envelope() {
        return new Envelope(IdempotencyKey.of("k1"), new EventMetadata().set(EventMetadataKey.SOURCE, "boe"), MediaTypes.JSON,
                "{\"price\":-1}".getBytes(StandardCharsets.UTF_8), EnvelopeStatus.CAPTURED, 0, FIRST_TIME, FIRST_TIME);
    }
}
