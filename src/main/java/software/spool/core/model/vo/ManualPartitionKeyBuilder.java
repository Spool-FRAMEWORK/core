package software.spool.core.model.vo;

public class ManualPartitionKeyBuilder {
    private final KeyEntries entries;

    ManualPartitionKeyBuilder(boolean withDate) {
        this.entries = withDate ? PartitionKeyBuilderUtils.dateEntries() : new KeyEntries();
    }

    public ManualPartitionKeyBuilder with(String key, Object value) {
        entries.with(key, value);
        return this;
    }

    public PartitionKey build() {
        return new PartitionKey(entries.format());
    }
}
