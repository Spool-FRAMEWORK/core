package software.spool.core.port.datalake;

import software.spool.core.exception.EnvelopeHeaderStoreException;
import software.spool.core.model.vo.EnvelopeHeader;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Finds the headers of the envelopes in the data lake without reading it.
 */
public interface EnvelopeHeaderReader {
    Optional<EnvelopeHeader> find(IdempotencyKey idempotencyKey) throws EnvelopeHeaderStoreException;

    /**
     * @return the headers of the envelopes in the partition, or in any partition below it
     */
    Stream<EnvelopeHeader> findIn(PartitionKey partition) throws EnvelopeHeaderStoreException;
}
