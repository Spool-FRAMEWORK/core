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
        Envelope updated = aCapturedEnvelope().withStatus(EnvelopeStatus.INGESTED);
        assertThat(updated.status()).isEqualTo(EnvelopeStatus.INGESTED);
    }

    @Test
    void withStatus_doesNotMutateOriginal() {
        Envelope original = aCapturedEnvelope();
        original.withStatus(EnvelopeStatus.INGESTED);
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
}
