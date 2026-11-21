package software.spool.core.utils.datalake;

import org.junit.jupiter.api.Test;
import software.spool.core.exception.DataLakeRemovalException;
import software.spool.core.exception.EnvelopeHeaderStoreException;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.port.datalake.DataLakeEnvelopeRemover;
import software.spool.core.port.datalake.EnvelopeHeaderRemover;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IndexedDataLakeEnvelopeRemoverTest {
    private static final PartitionKey PARTITION = PartitionKey.ofEntriesWithoutDate().with("source", "payments").build();
    private static final IdempotencyKey KEY = IdempotencyKey.of("k1");

    private final List<String> order = new ArrayList<>();
    private final DataLakeEnvelopeRemover recordingDataLake = (partition, key) -> order.add("data lake");

    @Test
    void remove_envelope_removesItsHeaderBeforeRemovingItFromTheDataLake() {
        EnvelopeHeaderRemover recordingHeaders = (partition, key) -> order.add("headers");

        new IndexedDataLakeEnvelopeRemover(recordingHeaders, recordingDataLake).remove(PARTITION, KEY);

        assertThat(order).containsExactly("headers", "data lake");
    }

    @Test
    void remove_headerTheStoreCannotRemove_failsAndLeavesTheDataLakeUntouched() {
        EnvelopeHeaderRemover failing = (partition, key) -> {
            throw new EnvelopeHeaderStoreException("store unavailable", new IllegalStateException());
        };

        assertThatThrownBy(() -> new IndexedDataLakeEnvelopeRemover(failing, recordingDataLake).remove(PARTITION, KEY))
                .isInstanceOf(DataLakeRemovalException.class)
                .hasMessageContaining("k1");
        assertThat(order).isEmpty();
    }
}
