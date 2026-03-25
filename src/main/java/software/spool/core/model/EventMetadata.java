package software.spool.core.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record EventMetadata(Map<EventMetadataKey, String> metadata) {

    public EventMetadata() {
        this(new HashMap<>());
    }

    public String get(final EventMetadataKey key) {
        return metadata.get(key);
    }

    public EventMetadata set(final EventMetadataKey key, final String value) {
        metadata.put(key, value);
        return this;
    }

    public Set<Map.Entry<EventMetadataKey, String>> entrySet() {
        return metadata.entrySet();
    }
}
