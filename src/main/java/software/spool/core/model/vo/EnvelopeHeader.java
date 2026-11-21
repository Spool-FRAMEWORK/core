package software.spool.core.model.vo;

import software.spool.core.model.EnvelopeStatus;

import java.time.Instant;

/**
 * An envelope without its payload: what the data lake keeps next to the payload, and what a store of envelope headers
 * keeps to find envelopes without reading the data lake.
 */
public record EnvelopeHeader(
        IdempotencyKey idempotencyKey,
        EventMetadata metadata,
        MediaType mediaType,
        EnvelopeStatus status,
        int retries,
        Instant capturedAt,
        Instant updatedAt
) {
}
