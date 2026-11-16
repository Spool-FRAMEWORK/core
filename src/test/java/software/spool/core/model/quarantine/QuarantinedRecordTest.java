package software.spool.core.model.quarantine;

import org.junit.jupiter.api.Test;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.EventMetadata;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.utils.media.MediaTypes;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuarantinedRecordTest {

    private static final Instant FIRST_TIME = Instant.parse("2026-11-01T10:00:00Z");
    private static final Instant SECOND_TIME = Instant.parse("2026-11-02T10:00:00Z");
    private static final Instant THIRD_TIME = Instant.parse("2026-11-03T10:00:00Z");

    @Test
    void pending_newRecord_isPendingWithOneEntry() {
        QuarantinedRecord quarantinedRecord = pendingRecord();

        assertThat(quarantinedRecord.status()).isEqualTo(QuarantineStatus.PENDING);
        assertThat(quarantinedRecord.history()).hasSize(1);
        assertThat(quarantinedRecord.reason()).isEqualTo(QuarantineReason.VALIDATION_FAILED);
        assertThat(quarantinedRecord.violations()).containsExactly("price must be positive");
        assertThat(quarantinedRecord.quarantinedAt()).isEqualTo(FIRST_TIME);
        assertThat(quarantinedRecord.resolution().resolvedAt()).isEmpty();
    }

    @Test
    void resolve_pendingRecord_closesItsLastEntry() {
        QuarantinedRecord resolved = pendingRecord().resolve(QuarantineResolution.republished(SECOND_TIME));

        assertThat(resolved.status()).isEqualTo(QuarantineStatus.REPUBLISHED);
        assertThat(resolved.resolution().resolvedAt()).contains(SECOND_TIME);
        assertThat(resolved.history()).hasSize(1);
    }

    @Test
    void resolve_corrected_keepsTheKeyOfTheCorrectedEnvelope() {
        QuarantinedRecord resolved = pendingRecord()
                .resolve(QuarantineResolution.corrected(SECOND_TIME, IdempotencyKey.of("corrected-key")));

        assertThat(resolved.resolution()).isInstanceOfSatisfying(QuarantineResolution.Corrected.class,
                corrected -> assertThat(corrected.correctedTo()).isEqualTo(IdempotencyKey.of("corrected-key")));
    }

    @Test
    void resolve_bypassed_isBypassedWithItsReason() {
        QuarantinedRecord resolved = pendingRecord()
                .resolve(QuarantineResolution.bypassed(SECOND_TIME, "price rule misconfigured"));

        assertThat(resolved.status()).isEqualTo(QuarantineStatus.BYPASSED);
        assertThat(resolved.resolution()).isInstanceOfSatisfying(QuarantineResolution.Bypassed.class,
                bypassed -> assertThat(bypassed.reason()).isEqualTo("price rule misconfigured"));
    }

    @Test
    void bypassed_blankReason_fails() {
        assertThatThrownBy(() -> QuarantineResolution.bypassed(SECOND_TIME, " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reason");
    }

    @Test
    void resolve_alreadyResolvedRecord_fails() {
        QuarantinedRecord resolved = pendingRecord().resolve(QuarantineResolution.purged(SECOND_TIME));

        assertThatThrownBy(() -> resolved.resolve(QuarantineResolution.republished(THIRD_TIME)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("k1")
                .hasMessageContaining("PURGED");
    }

    @Test
    void reenter_resolvedRecord_isPendingAgainWithTwoEntries() {
        QuarantinedRecord reopened = pendingRecord()
                .resolve(QuarantineResolution.republished(SECOND_TIME))
                .reenter(QuarantineReason.RETRIES_EXHAUSTED, List.of(), THIRD_TIME);

        assertThat(reopened.status()).isEqualTo(QuarantineStatus.PENDING);
        assertThat(reopened.reason()).isEqualTo(QuarantineReason.RETRIES_EXHAUSTED);
        assertThat(reopened.quarantinedAt()).isEqualTo(THIRD_TIME);
        assertThat(reopened.history()).extracting(entry -> entry.resolution().status())
                .containsExactly(QuarantineStatus.REPUBLISHED, QuarantineStatus.PENDING);
    }

    @Test
    void reenter_pendingRecord_staysAsItWas() {
        QuarantinedRecord pending = pendingRecord();

        QuarantinedRecord again = pending.reenter(QuarantineReason.VALIDATION_FAILED, List.of("other"), SECOND_TIME);

        assertThat(again).isEqualTo(pending);
    }

    @Test
    void new_withoutHistory_fails() {
        assertThatThrownBy(() -> new QuarantinedRecord(envelope(), QuarantineLocation.inbox(), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("history");
    }

    private static QuarantinedRecord pendingRecord() {
        return QuarantinedRecord.pending(envelope(), QuarantineLocation.inbox(), QuarantineReason.VALIDATION_FAILED,
                List.of("price must be positive"), FIRST_TIME);
    }

    private static Envelope envelope() {
        return new Envelope(IdempotencyKey.of("k1"), new EventMetadata(), MediaTypes.JSON,
                "{\"price\":-1}".getBytes(StandardCharsets.UTF_8), EnvelopeStatus.CAPTURED, 0, FIRST_TIME, FIRST_TIME);
    }
}
