package software.spool.core.model.vo;

import software.spool.core.model.EnvelopeStatus;

import java.time.Instant;

public record Envelope(
        IdempotencyKey idempotencyKey,
        EventMetadata metadata,
        MediaType mediaType,
        String payload,
        EnvelopeStatus status,
        int retries,
        Instant capturedAt,
        Instant updatedAt
) {

    public Envelope withStatus(final EnvelopeStatus status) {
        return new Envelope(
                this.idempotencyKey,
                this.metadata,
                this.mediaType,
                this.payload,
                status,
                this.retries,
                this.capturedAt,
                Instant.now()
        );
    }

    public Envelope retry() {
        return new Envelope(
                this.idempotencyKey,
                this.metadata,
                this.mediaType,
                this.payload,
                this.status,
                this.retries + 1,
                this.capturedAt,
                Instant.now()
        );
    }
}
