package software.spool.core.port.inbox;

import org.junit.jupiter.api.Test;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.EventMetadata;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.utils.media.MediaTypes;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InboxStatusQueryTest {

    private static final Instant LIMIT = Instant.parse("2026-09-10T00:00:00Z");

    private final Envelope old = envelope("old", Instant.parse("2026-09-01T00:00:00Z"), null);
    private final Envelope recent = envelope("recent", Instant.parse("2026-09-11T00:00:00Z"), null);
    private final Envelope capturedLongAgoButUpdatedRecently = envelope("touched",
            Instant.parse("2026-09-01T00:00:00Z"), Instant.parse("2026-09-12T00:00:00Z"));
    private final Envelope updatedLongAgo = envelope("stale",
            Instant.parse("2026-09-15T00:00:00Z"), Instant.parse("2026-09-05T00:00:00Z"));

    private final InboxStatusQuery query = status ->
            List.of(old, recent, capturedLongAgoButUpdatedRecently, updatedLongAgo);

    @Test
    void findByStatusModifiedBefore_byDefault_keepsWhatLastChangedBeforeTheLimit() {
        Collection<Envelope> found = query.findByStatusModifiedBefore(EnvelopeStatus.PERSISTED, LIMIT);

        assertThat(found).containsExactly(old, updatedLongAgo);
    }

    @Test
    void findByStatusModifiedBefore_nothingOldEnough_isEmpty() {
        assertThat(query.findByStatusModifiedBefore(EnvelopeStatus.PERSISTED, Instant.parse("2026-08-01T00:00:00Z")))
                .isEmpty();
    }

    @Test
    void findByStatusModifiedBefore_usesTheUpdateDateWhenThereIsOne() {
        assertThat(query.findByStatusModifiedBefore(EnvelopeStatus.PERSISTED, LIMIT))
                .doesNotContain(capturedLongAgoButUpdatedRecently);
    }

    private static Envelope envelope(String key, Instant capturedAt, Instant updatedAt) {
        return new Envelope(
                IdempotencyKey.of(key),
                new EventMetadata(),
                MediaTypes.JSON,
                "{}".getBytes(StandardCharsets.UTF_8),
                EnvelopeStatus.PERSISTED,
                0,
                capturedAt,
                updatedAt);
    }
}
