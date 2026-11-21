package software.spool.core.adapter.jackson;

import org.junit.jupiter.api.Test;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.quarantine.QuarantineLocation;
import software.spool.core.model.quarantine.QuarantineReason;
import software.spool.core.model.quarantine.QuarantineResolution;
import software.spool.core.model.quarantine.Resolver;
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
                .resolve(QuarantineResolution.corrected(SECOND_TIME, IdempotencyKey.of("corrected-key"), Resolver.identified("ana", "people")))
                .reenter(QuarantineReason.VALIDATION_FAILED, List.of("again"), SECOND_TIME);

        QuarantinedRecord read = roundTrip(original);

        assertThat(read.location()).isEqualTo(QuarantineLocation.bronze(partition));
        assertThat(read.history()).isEqualTo(original.history());
    }

    @Test
    void roundTrip_bypassedRecord_keepsItsReason() {
        QuarantinedRecord original = QuarantinedRecord.pending(envelope(), QuarantineLocation.inbox(),
                        QuarantineReason.VALIDATION_FAILED, List.of(), FIRST_TIME)
                .resolve(QuarantineResolution.bypassed(SECOND_TIME, "price rule misconfigured", Resolver.identified("ana", "people")));

        QuarantinedRecord read = roundTrip(original);

        assertThat(read.resolution()).isEqualTo(QuarantineResolution.bypassed(SECOND_TIME, "price rule misconfigured", Resolver.identified("ana", "people")));
    }

    @Test
    void read_recordResolvedBeforeResolversWereKept_hasAnUnknownResolver() {
        QuarantinedRecord resolved = QuarantinedRecord.pending(envelope(), QuarantineLocation.inbox(),
                        QuarantineReason.VALIDATION_FAILED, List.of(), FIRST_TIME)
                .resolve(QuarantineResolution.purged(SECOND_TIME, Resolver.identified("ana", "people")));
        String withoutResolver = new String(RecordSerializerFactory.<QuarantinedRecord>record().serialize(resolved), StandardCharsets.UTF_8)
                .replaceAll(",\"by\":\\{[^}]*\\}", "");

        QuarantinedRecord read = PayloadDeserializerFactory.json().as(QuarantinedRecord.class)
                .deserialize(withoutResolver.getBytes(StandardCharsets.UTF_8));

        assertThat(withoutResolver).doesNotContain("\"by\"");
        assertThat(read.resolution()).isEqualTo(QuarantineResolution.purged(SECOND_TIME, Resolver.unknown()));
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
