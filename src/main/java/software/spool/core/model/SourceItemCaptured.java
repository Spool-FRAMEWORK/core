package software.spool.core.model;

import java.time.Instant;
import java.util.UUID;

public record SourceItemCaptured(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        String crawlerId,
        String sourceId
) implements SpoolEvent {
    public static class Builder {
        private String correlationId;
        private String causationId;
        private String crawlerId;
        private String sourceId;

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder causationId(String causationId) {
            this.causationId = causationId;
            return this;
        }

        public Builder crawlerId(String crawlerId) {
            this.crawlerId = crawlerId;
            return this;
        }

        public Builder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public SourceItemCaptured build() {
            return new SourceItemCaptured(
                    UUID.randomUUID().toString(),
                    Instant.now(),
                    correlationId,
                    causationId,
                    crawlerId,
                    sourceId
            );
        }
    }
}
