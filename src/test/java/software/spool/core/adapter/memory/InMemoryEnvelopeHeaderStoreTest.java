package software.spool.core.adapter.memory;

import org.junit.jupiter.api.Test;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.EnvelopeHeader;
import software.spool.core.model.vo.EventMetadata;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.MediaType;
import software.spool.core.model.vo.PartitionKey;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryEnvelopeHeaderStoreTest {
    private static final PartitionKey PAYMENTS = PartitionKey.ofEntriesWithoutDate().with("source", "payments").with("day", "21").build();
    private static final PartitionKey REFUNDS = PartitionKey.ofEntriesWithoutDate().with("source", "refunds").with("day", "21").build();

    private final InMemoryEnvelopeHeaderStore store = new InMemoryEnvelopeHeaderStore();

    @Test
    void find_storedHeader_returnsIt() {
        store.write(PAYMENTS, header("k1"));

        assertThat(store.find(IdempotencyKey.of("k1"))).contains(header("k1"));
    }

    @Test
    void findIn_partition_returnsTheHeadersOfThatPartitionAndBelowIt() {
        store.write(PAYMENTS, header("k1"));
        store.write(REFUNDS, header("k2"));

        assertThat(store.findIn(PartitionKey.ofEntriesWithoutDate().with("source", "payments").build()))
                .containsExactly(header("k1"));
    }

    @Test
    void remove_storedHeader_removesItOnlyFromThatPartition() {
        store.write(PAYMENTS, header("k1"));

        store.remove(REFUNDS, IdempotencyKey.of("k1"));
        assertThat(store.find(IdempotencyKey.of("k1"))).isPresent();

        store.remove(PAYMENTS, IdempotencyKey.of("k1"));
        assertThat(store.find(IdempotencyKey.of("k1"))).isEmpty();
    }

    private static EnvelopeHeader header(String key) {
        Instant at = Instant.parse("2026-11-21T10:00:00Z");
        return new EnvelopeHeader(IdempotencyKey.of(key), new EventMetadata(), MediaType.of("application/json"),
                EnvelopeStatus.PERSISTED, 0, at, at);
    }
}
