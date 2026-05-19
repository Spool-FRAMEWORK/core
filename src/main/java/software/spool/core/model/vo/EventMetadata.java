package software.spool.core.model.vo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public record EventMetadata(Map<EventMetadataKey, byte[]> metadata) {

    public EventMetadata() {
        this(new HashMap<>());
    }

    public byte[] get(final EventMetadataKey key) {
        return metadata.get(key);
    }

    public EventMetadata set(final EventMetadataKey key, final byte[] value) {
        metadata.put(key, value);
        return this;
    }

    public Set<Map.Entry<EventMetadataKey, byte[]>> entrySet() {
        return metadata.entrySet();
    }
}
