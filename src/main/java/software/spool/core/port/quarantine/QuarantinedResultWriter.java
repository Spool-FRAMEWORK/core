package software.spool.core.port.quarantine;

import software.spool.core.exception.QuarantinedResultRejectedException;
import software.spool.core.model.vo.PartitionKey;

/**
 * Writes a mount result taken out of quarantine to the data mart it belongs to, in the partition that data mart
 * chooses for it now.
 */
public interface QuarantinedResultWriter {
    /**
     * The data mart it writes to, named with its layer ({@code gold/payments}).
     */
    String dataMart();

    /**
     * @param partition the partition the result came with
     * @param content   the result, as the JSON it was kept in quarantine with
     * @throws QuarantinedResultRejectedException if the result still cannot be written
     */
    void write(PartitionKey partition, byte[] content) throws QuarantinedResultRejectedException;
}
