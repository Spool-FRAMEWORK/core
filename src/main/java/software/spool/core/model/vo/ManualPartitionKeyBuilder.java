package software.spool.core.model.vo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ManualPartitionKeyBuilder {
    private final Map<String, Object> entries = new LinkedHashMap<>();

    ManualPartitionKeyBuilder() {}

    public ManualPartitionKeyBuilder with(String key, Object value) {
        entries.put(key, value);
        return this;
    }

    public PartitionKey build() {
        String base = PartitionKeyBuilderUtils.datePrefix();
        if (entries.isEmpty()) return new PartitionKey(base);
        String parts = entries.entrySet().stream()
                .map(e -> e.getKey() + "=" + PartitionKeyBuilderUtils.escapeValue(e.getValue()))
                .collect(Collectors.joining("::"));
        return new PartitionKey(base + "::" + parts);
    }
}