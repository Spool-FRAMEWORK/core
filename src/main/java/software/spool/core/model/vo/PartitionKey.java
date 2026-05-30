package software.spool.core.model.vo;

import java.util.Objects;

public record PartitionKey(String value) {
    public PartitionKey {
        Objects.requireNonNull(value);
    }

    public static PartitionKey ofNow() {
        return new PartitionKey(PartitionKeyBuilderUtils.datePrefix());
    }

    public static ManualPartitionKeyBuilder ofEntries() {
        return new ManualPartitionKeyBuilder();
    }

    public static AutomaticBuilder of(PartitionKeySchema schema) {
        return new AutomaticBuilder(schema);
    }
}