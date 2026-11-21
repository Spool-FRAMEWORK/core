package software.spool.core.adapter.memory;

import software.spool.core.model.vo.EnvelopeHeader;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.port.datalake.EnvelopeHeaderReader;
import software.spool.core.port.datalake.EnvelopeHeaderRemover;
import software.spool.core.port.datalake.EnvelopeHeaderWriter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * Store of envelope headers kept in memory, for tests and single-process runs. Its headers are lost when the process
 * stops.
 */
public class InMemoryEnvelopeHeaderStore implements EnvelopeHeaderWriter, EnvelopeHeaderReader, EnvelopeHeaderRemover {
    private final ConcurrentHashMap<IdempotencyKey, PartitionedHeader> headers = new ConcurrentHashMap<>();

    @Override
    public void write(PartitionKey partition, EnvelopeHeader header) {
        headers.put(header.idempotencyKey(), new PartitionedHeader(partition, header));
    }

    @Override
    public Optional<EnvelopeHeader> find(IdempotencyKey idempotencyKey) {
        return Optional.ofNullable(headers.get(idempotencyKey)).map(PartitionedHeader::header);
    }

    @Override
    public Stream<EnvelopeHeader> findIn(PartitionKey partition) {
        List<EnvelopeHeader> found = headers.values().stream()
                .filter(stored -> stored.partition().contains(partition))
                .map(PartitionedHeader::header)
                .toList();
        return found.stream();
    }

    @Override
    public void remove(PartitionKey partition, IdempotencyKey idempotencyKey) {
        headers.computeIfPresent(idempotencyKey, (key, stored) -> stored.partition().equals(partition) ? null : stored);
    }

    private record PartitionedHeader(PartitionKey partition, EnvelopeHeader header) {
    }
}
