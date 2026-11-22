package software.spool.core.model.quarantine;

import software.spool.core.model.vo.PartitionKey;

import java.util.Objects;

/**
 * Where the envelope stayed when it was quarantined, which is where resolving it sends it back to.
 */
public sealed interface QuarantineLocation {

    static QuarantineLocation inbox() {
        return new Inbox();
    }

    static QuarantineLocation bronze(PartitionKey partition) {
        return new Bronze(partition);
    }

    static QuarantineLocation dataMart(String dataMart, PartitionKey partition) {
        return new DataMart(dataMart, partition);
    }

    /**
     * The envelope never reached the data lake: the ingester rejected it or it ran out of retries.
     */
    record Inbox() implements QuarantineLocation {
    }

    /**
     * The envelope was already in bronze, in this partition, when the mounter rejected it.
     */
    record Bronze(PartitionKey partition) implements QuarantineLocation {
        public Bronze {
            Objects.requireNonNull(partition, "partition is required");
        }
    }

    /**
     * The envelope holds a mount result that could not be written to the data mart {@code dataMart}, named with its
     * layer ({@code gold/payments}). {@code partition} is the one the result came with.
     */
    record DataMart(String dataMart, PartitionKey partition) implements QuarantineLocation {
        public DataMart {
            Objects.requireNonNull(dataMart, "dataMart is required");
            Objects.requireNonNull(partition, "partition is required");
        }
    }
}
