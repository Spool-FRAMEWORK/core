package software.spool.core.adapter.memory;

import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.port.datalake.BronzeEnvelopeRemover;
import software.spool.core.port.datalake.BronzeWriter;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bronze layer kept in memory, for tests and single-process runs. Its records are lost when the process stops.
 */
public class InMemoryBronze implements BronzeWriter, BronzeEnvelopeRemover {
    private final ConcurrentHashMap<Location, Envelope> records = new ConcurrentHashMap<>();

    @Override
    public void save(PartitionKey partition, Envelope envelope) {
        records.put(new Location(partition, envelope.idempotencyKey()), envelope);
    }

    @Override
    public void remove(PartitionKey partition, IdempotencyKey idempotencyKey) {
        records.remove(new Location(partition, idempotencyKey));
    }

    public Optional<Envelope> find(PartitionKey partition, IdempotencyKey idempotencyKey) {
        return Optional.ofNullable(records.get(new Location(partition, idempotencyKey)));
    }

    private record Location(PartitionKey partition, IdempotencyKey idempotencyKey) {
    }
}
