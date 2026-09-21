package software.spool.core.model.vo;

import org.junit.jupiter.api.Test;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.utils.media.MediaTypes;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EnvelopeLastModifiedTest {

    private static final Instant CAPTURED_AT = Instant.parse("2026-09-01T10:00:00Z");
    private static final Instant UPDATED_AT = Instant.parse("2026-09-02T12:00:00Z");

    @Test
    void lastModifiedAt_neverUpdated_isTheCaptureDate() {
        assertThat(envelope(null).lastModifiedAt()).isEqualTo(CAPTURED_AT);
    }

    @Test
    void lastModifiedAt_updated_isTheUpdateDate() {
        assertThat(envelope(UPDATED_AT).lastModifiedAt()).isEqualTo(UPDATED_AT);
    }

    @Test
    void lastModifiedAt_afterMovingToAnotherStatus_isTheMomentOfTheMove() {
        Envelope moved = envelope(null).withStatus(EnvelopeStatus.PERSISTED);

        assertThat(moved.lastModifiedAt()).isAfter(CAPTURED_AT);
        assertThat(moved.lastModifiedAt()).isEqualTo(moved.updatedAt());
    }

    private static Envelope envelope(Instant updatedAt) {
        return new Envelope(
                IdempotencyKey.of("key"),
                new EventMetadata(),
                MediaTypes.JSON,
                "{}".getBytes(StandardCharsets.UTF_8),
                EnvelopeStatus.CAPTURED,
                0,
                CAPTURED_AT,
                updatedAt);
    }
}
