package software.spool.core.adapter.memory;

import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.model.vo.PartitionedEnvelope;
import software.spool.core.port.datalake.DataLakeEnvelopeRemover;
import software.spool.core.port.datalake.DataLakeWriter;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Data lake kept in memory, for tests and single-process runs. Its envelopes are lost when the process stops.
 */
public class InMemoryDataLake implements DataLakeWriter, DataLakeEnvelopeRemover {
    private final ConcurrentHashMap<Location, Envelope> envelopes = new ConcurrentHashMap<>();

    @Override
    public Stream<IdempotencyKey> write(Collection<PartitionedEnvelope> items) {
        items.forEach(item -> envelopes.put(new Location(item.partition(), item.envelope().idempotencyKey()), item.envelope()));
        return items.stream().map(item -> item.envelope().idempotencyKey()).toList().stream();
    }

    @Override
    public void remove(PartitionKey partition, IdempotencyKey idempotencyKey) {
        envelopes.remove(new Location(partition, idempotencyKey));
    }

    public Optional<Envelope> find(PartitionKey partition, IdempotencyKey idempotencyKey) {
        return Optional.ofNullable(envelopes.get(new Location(partition, idempotencyKey)));
    }

    private record Location(PartitionKey partition, IdempotencyKey idempotencyKey) {
    }
}
