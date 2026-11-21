package software.spool.core.model.quarantine;

import org.junit.jupiter.api.Test;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.EventMetadata;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.MediaType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuarantinePageTest {
    private static final Instant AT = Instant.parse("2026-11-21T10:00:00Z");

    private final List<QuarantinedRecord> records = List.of(pending("c"), pending("a"), purged("b"), pending("d"));

    @Test
    void of_firstPage_hasTheFirstRecordsOfTheStatusInKeyOrderAndTheKeyToGoOn() {
        QuarantinePage page = QuarantinePage.of(records, QuarantinePageRequest.first(QuarantineStatus.PENDING, 2));

        assertThat(page.records()).extracting(record -> record.idempotencyKey().value()).containsExactly("a", "c");
        assertThat(page.next()).contains(IdempotencyKey.of("c"));
    }

    @Test
    void of_lastPage_hasTheRestAndNoKeyToGoOn() {
        QuarantinePage page = QuarantinePage.of(records,
                new QuarantinePageRequest(QuarantineStatus.PENDING, 2, Optional.of(IdempotencyKey.of("c"))));

        assertThat(page.records()).extracting(record -> record.idempotencyKey().value()).containsExactly("d");
        assertThat(page.next()).isEmpty();
    }

    @Test
    void of_pageThatEndsExactlyWithTheRecords_hasNoKeyToGoOn() {
        QuarantinePage page = QuarantinePage.of(records, QuarantinePageRequest.first(QuarantineStatus.PENDING, 3));

        assertThat(page.records()).hasSize(3);
        assertThat(page.next()).isEmpty();
    }

    @Test
    void request_withoutRoomForARecord_isRejected() {
        assertThatThrownBy(() -> QuarantinePageRequest.first(QuarantineStatus.PENDING, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static QuarantinedRecord pending(String key) {
        return QuarantinedRecord.pending(new Envelope(IdempotencyKey.of(key), new EventMetadata(), MediaType.of("application/json"),
                "{}".getBytes(), EnvelopeStatus.QUARANTINED, 0, AT, AT), QuarantineLocation.inbox(),
                QuarantineReason.VALIDATION_FAILED, List.of(), AT);
    }

    private static QuarantinedRecord purged(String key) {
        return pending(key).resolve(QuarantineResolution.purged(AT, Resolver.unknown()));
    }
}
