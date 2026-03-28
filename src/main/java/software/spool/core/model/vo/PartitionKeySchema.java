package software.spool.core.model.vo;

import java.util.List;
import java.util.Objects;

public record PartitionKeySchema(String sourceId, Class<?> eventType, List<String> attributes) {
    public PartitionKeySchema(final String sourceId, final Class<?> eventType, final List<String> attributes) {
        this.sourceId = Objects.requireNonNull(sourceId);
        this.eventType = Objects.requireNonNullElse(eventType, Void.class);
        this.attributes = Objects.isNull(attributes) ? List.of() : List.copyOf(attributes);
    }

    public static PartitionKeySchema of(final String sourceId, final Class<?> eventType, final List<String> attributes) {
        return new PartitionKeySchema(sourceId, eventType, attributes);
    }

    public static PartitionKeySchema of(final String sourceId, final Class<?> eventType) {
        return new PartitionKeySchema(sourceId, eventType, List.of());
    }

    public String value() {
        String base = Objects.isNull(eventType)
                ? sourceId
                : String.format("%s::%s", sourceId, eventType.getSimpleName());
        return attributes.isEmpty() ? base : base + "::" + String.join("::", attributes);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String sourceId;
        private Class<?> eventType;
        private List<String> attributes;

        public Builder withSourceId(final String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder withEventType(final Class<?> eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder withAttributes(final List<String> attributes) {
            this.attributes = attributes;
            return this;
        }

        public PartitionKeySchema build() {
            return PartitionKeySchema.of(sourceId, eventType, attributes);
        }
    }
}
