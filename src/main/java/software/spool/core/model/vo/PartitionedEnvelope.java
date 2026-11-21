package software.spool.core.model.vo;

import java.util.Objects;

public record PartitionedEnvelope(PartitionKey partition, Envelope envelope) {
    public PartitionedEnvelope {
        Objects.requireNonNull(partition);
        Objects.requireNonNull(envelope);
    }
}
