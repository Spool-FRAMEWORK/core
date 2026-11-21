package software.spool.core.model.vo;

import org.junit.jupiter.api.Test;
import software.spool.core.model.EnvelopeStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class EnvelopeTest {

    private Envelope aCapturedEnvelope() {
        return new Envelope(
                IdempotencyKey.of("key-1"),
                new EventMetadata(),
                MediaType.of("application/json"),
                "{}".getBytes(),
                EnvelopeStatus.CAPTURED,
                0,
                Instant.now(),
                Instant.now()
        );
    }

    @Test
    void withStatus_returnsNewEnvelopeWithUpdatedStatus() {
        Envelope updated = aCapturedEnvelope().withStatus(EnvelopeStatus.PERSISTED);
        assertThat(updated.status()).isEqualTo(EnvelopeStatus.PERSISTED);
    }

    @Test
    void withStatus_doesNotMutateOriginal() {
        Envelope original = aCapturedEnvelope();
        original.withStatus(EnvelopeStatus.PERSISTED);
        assertThat(original.status()).isEqualTo(EnvelopeStatus.CAPTURED);
    }

    @Test
    void retry_incrementsRetryCount() {
        assertThat(aCapturedEnvelope().retry().retries()).isEqualTo(1);
    }

    @Test
    void retry_doesNotMutateOriginal() {
        Envelope original = aCapturedEnvelope();
        original.retry();
        assertThat(original.retries()).isEqualTo(0);
    }

    @Test
    void header_keepsEverythingButThePayload() {
        Envelope envelope = aCapturedEnvelope();

        EnvelopeHeader header = envelope.header();

        assertThat(header).isEqualTo(new EnvelopeHeader(envelope.idempotencyKey(), envelope.metadata(), envelope.mediaType(),
                envelope.status(), envelope.retries(), envelope.capturedAt(), envelope.updatedAt()));
    }

    @Test
    void of_headerAndPayload_rebuildsTheEnvelope() {
        Envelope envelope = aCapturedEnvelope();

        Envelope rebuilt = Envelope.of(envelope.header(), envelope.payload());

        assertThat(rebuilt.header()).isEqualTo(envelope.header());
        assertThat(rebuilt.payload()).isEqualTo(envelope.payload());
    }
}
