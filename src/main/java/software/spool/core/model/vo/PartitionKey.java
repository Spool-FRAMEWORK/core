package software.spool.core.model.vo;

import java.util.Arrays;
import java.util.Objects;

public record PartitionKey(String value) {
    public PartitionKey {
        Objects.requireNonNull(value);
    }

    public static PartitionKey ofNow() {
        return new PartitionKey(PartitionKeyBuilderUtils.datePrefix());
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
        return Arrays.stream(scope.value().split("::"))
                     .allMatch(pair -> this.value().contains(pair));
    }
}