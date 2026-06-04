package software.spool.core.exception;

import software.spool.core.model.vo.PartitionKey;

public class PartitionSplitException extends SpoolException {
    public PartitionSplitException(PartitionKey key, String message) {
        super("Partition split failed for: " + key.value() + ". " + message);
    }
}
