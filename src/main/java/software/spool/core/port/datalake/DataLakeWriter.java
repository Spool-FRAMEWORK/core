package software.spool.core.port.datalake;

import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionedEnvelope;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Writes whole envelopes to the data lake, each one in its partition. Writing an envelope that already exists
 * replaces it.
 */
@FunctionalInterface
public interface DataLakeWriter {
    /**
     * @return the idempotency keys of the envelopes that were written; an envelope that could not be written is
     * left out
     */
    Stream<IdempotencyKey> write(Collection<PartitionedEnvelope> items);
}
