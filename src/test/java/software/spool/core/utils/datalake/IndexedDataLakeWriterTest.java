package software.spool.core.utils.datalake;

import org.junit.jupiter.api.Test;
import software.spool.core.adapter.memory.InMemoryDataLake;
import software.spool.core.adapter.memory.InMemoryEnvelopeHeaderStore;
import software.spool.core.exception.EnvelopeHeaderStoreException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.EventMetadata;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.MediaType;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.model.vo.PartitionedEnvelope;
import software.spool.core.port.datalake.DataLakeWriter;
import software.spool.core.port.datalake.EnvelopeHeaderWriter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class IndexedDataLakeWriterTest {
    private static final PartitionKey PARTITION = PartitionKey.ofEntriesWithoutDate().with("source", "payments").build();

    private final InMemoryDataLake dataLake = new InMemoryDataLake();
    private final InMemoryEnvelopeHeaderStore headers = new InMemoryEnvelopeHeaderStore();

    @Test
    void write_envelopes_writesThemInTheDataLakeAndStoresTheirHeaders() {
        List<IdempotencyKey> written = new IndexedDataLakeWriter(dataLake, headers)
                .write(List.of(item("k1"), item("k2"))).toList();

        assertThat(written).containsExactly(IdempotencyKey.of("k1"), IdempotencyKey.of("k2"));
        assertThat(dataLake.find(PARTITION, IdempotencyKey.of("k1"))).isPresent();
        assertThat(headers.find(IdempotencyKey.of("k1"))).contains(envelope("k1").header());
        assertThat(headers.findIn(PARTITION)).hasSize(2);
    }

    @Test
    void write_envelope_writesInTheDataLakeBeforeStoringItsHeader() {
        List<String> order = new ArrayList<>();
        DataLakeWriter recordingDataLake = items -> {
            order.add("data lake");
            return items.stream().map(item -> item.envelope().idempotencyKey()).toList().stream();
        };
        EnvelopeHeaderWriter recordingHeaders = (partition, header) -> order.add("headers");

        new IndexedDataLakeWriter(recordingDataLake, recordingHeaders).write(List.of(item("k1"))).toList();

        assertThat(order).containsExactly("data lake", "headers");
    }

    @Test
    void write_envelopeTheDataLakeDoesNotWrite_leavesItOutAndDoesNotStoreItsHeader() {
        DataLakeWriter writesOnlyK1 = items -> Stream.of(IdempotencyKey.of("k1"));

        List<IdempotencyKey> written = new IndexedDataLakeWriter(writesOnlyK1, headers)
                .write(List.of(item("k1"), item("k2"))).toList();

        assertThat(written).containsExactly(IdempotencyKey.of("k1"));
        assertThat(headers.find(IdempotencyKey.of("k2"))).isEmpty();
    }

    @Test
    void write_headerTheStoreCannotKeep_leavesTheEnvelopeOut() {
        EnvelopeHeaderWriter failsForK2 = (partition, header) -> {
            if (header.idempotencyKey().equals(IdempotencyKey.of("k2"))) {
                throw new EnvelopeHeaderStoreException("store unavailable", new IllegalStateException());
            }
            headers.write(partition, header);
        };

        List<IdempotencyKey> written = new IndexedDataLakeWriter(dataLake, failsForK2)
                .write(List.of(item("k1"), item("k2"))).toList();

        assertThat(written).containsExactly(IdempotencyKey.of("k1"));
    }

    private static PartitionedEnvelope item(String key) {
        return new PartitionedEnvelope(PARTITION, envelope(key));
    }

    private static Envelope envelope(String key) {
        Instant at = Instant.parse("2026-11-21T10:00:00Z");
        return new Envelope(IdempotencyKey.of(key), new EventMetadata(), MediaType.of("application/json"),
                "{}".getBytes(StandardCharsets.UTF_8), EnvelopeStatus.PERSISTED, 0, at, at);
    }
}
