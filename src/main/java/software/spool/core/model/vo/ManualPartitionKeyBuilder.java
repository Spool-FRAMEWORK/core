package software.spool.core.model.vo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ManualPartitionKeyBuilder {
    private final Map<String, Object> entries = new LinkedHashMap<>();
    private final boolean withDate;

    ManualPartitionKeyBuilder(boolean withDate) {
        this.withDate = withDate;
    }

    public ManualPartitionKeyBuilder with(String key, Object value) {
        entries.put(key, value);
        return this;
    }

    public PartitionKey build() {
        String parts = entries.entrySet().stream()
                .map(e -> e.getKey() + "=" + PartitionKeyBuilderUtils.escapeValue(e.getValue()))
                .collect(Collectors.joining("::"));

        if (!withDate) return new PartitionKey(parts.isEmpty() ? "" : parts);

        String base = PartitionKeyBuilderUtils.datePrefix();
        return new PartitionKey(parts.isEmpty() ? base : base + "::" + parts);
    }
}