package software.spool.core.model.vo;

import software.spool.core.model.EnvelopeStatus;

import java.time.Instant;

public record Envelope(
        IdempotencyKey idempotencyKey,
        EventMetadata metadata,
        MediaType mediaType,
        byte[] payload,
        EnvelopeStatus status,
        int retries,
        Instant capturedAt,
        Instant updatedAt
) {

    /**
     * Returns when the envelope last changed.
     *
     * @return the update date, or the capture date when it has not been updated
     */
    public Instant lastModifiedAt() {
        return updatedAt != null ? updatedAt : capturedAt;
    }

    public EnvelopeHeader header() {
        return new EnvelopeHeader(idempotencyKey, metadata, mediaType, status, retries, capturedAt, updatedAt);
    }

    public static Envelope of(EnvelopeHeader header, byte[] payload) {
        return new Envelope(header.idempotencyKey(), header.metadata(), header.mediaType(), payload, header.status(),
                header.retries(), header.capturedAt(), header.updatedAt());
    }

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
