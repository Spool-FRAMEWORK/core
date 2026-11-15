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
}
