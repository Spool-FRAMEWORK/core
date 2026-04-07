package software.spool.core.port.serde;

import java.util.Objects;

public record EnrichmentRule(String source, String target) {
    public EnrichmentRule(String source, String target) {
        this.source = Objects.requireNonNull(source, "source cannot be null");
        this.target = Objects.requireNonNullElse(target, source);
    }

    public static EnrichmentRule copyField(String fieldName) {
        return new EnrichmentRule(fieldName, fieldName);
    }
}