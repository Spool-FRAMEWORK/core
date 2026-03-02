package software.spool.core.model;

import java.time.Instant;

public record SourceCaptureFailed(
        String eventId,
        Instant timestamp,
        String correlationId,
        String causationId,
        String crawlerId,
        String sourceId,
        String errorMessage
) implements SpoolEvent {
    public static class Builder {
        private String eventId;
        private Instant timestamp;
        private String correlationId;
        private String causationId;
        private String crawlerId;
        private String sourceId;
        private String errorMessage;

        public Builder eventId(final String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder timestamp(final Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder correlationId(final String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder causationId(final String causationId) {
            this.causationId = causationId;
            return this;
        }

        public Builder crawlerId(final String crawlerId) {
            this.crawlerId = crawlerId;
            return this;
        }

        public Builder sourceId(final String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder errorMessage(final String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public SourceCaptureFailed build() {
            return new SourceCaptureFailed(eventId, timestamp, correlationId, causationId, crawlerId, sourceId, errorMessage);
        }
    }
}
