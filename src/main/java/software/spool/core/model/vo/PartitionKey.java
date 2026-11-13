package software.spool.core.model.vo;

import java.util.Objects;

public record PartitionKey(String value) {
    public PartitionKey {
        Objects.requireNonNull(value);
    }

    public static PartitionKey ofNow() {
        return new PartitionKey(PartitionKeyBuilderUtils.dateEntries().format());
    }

    public static ManualPartitionKeyBuilder ofEntries() {
        return new ManualPartitionKeyBuilder(true);
    }

    public static ManualPartitionKeyBuilder ofEntriesWithoutDate() {
        return new ManualPartitionKeyBuilder(false);
    }

    public static AutomaticBuilder of(PartitionKeySchema schema) {
        return new AutomaticBuilder(schema);
    }

    public boolean contains(PartitionKey scope) {
        return KeyEntries.split(scope.value()).stream()
                .allMatch(entry -> this.value().contains(entry));
    }
}