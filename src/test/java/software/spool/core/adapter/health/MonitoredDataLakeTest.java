package software.spool.core.adapter.health;

import org.junit.jupiter.api.Test;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.port.datalake.DataLakeEnvelopeRemover;
import software.spool.core.port.datalake.DataLakeWriter;
import software.spool.core.port.health.HealthStatus;
import software.spool.core.port.health.PieceCheckPolicy;
import software.spool.core.port.health.PieceMonitor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonitoredDataLakeTest {
    private static final PartitionKey PARTITION = new PartitionKey("source=payments");

    private final PieceMonitor monitor = PieceMonitor.of("lake", "S3", Optional.empty(), PieceCheckPolicy.defaults());

    @Test
    void writer_givesTheKeysItWrote() {
        DataLakeWriter writer = new MonitoredDataLakeWriter(items -> Stream.of(IdempotencyKey.of("payment-1")), monitor);

        assertThat(writer.write(List.of()).toList()).containsExactly(IdempotencyKey.of("payment-1"));
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    void writer_whenAWriteFailsWhileItsKeysAreRead_marksThePieceUnhealthy() {
        DataLakeWriter writer = new MonitoredDataLakeWriter(items -> Stream.<IdempotencyKey>of(IdempotencyKey.of("payment-1"))
                .map(key -> {
                    throw new IllegalStateException("the bucket refused the object");
                }), monitor);

        assertThatThrownBy(() -> writer.write(List.of()).toList()).hasMessage("the bucket refused the object");
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.UNHEALTHY);
    }

    @Test
    void remover_whenTheDataLakeFails_marksThePieceUnhealthy() {
        DataLakeEnvelopeRemover remover = new MonitoredDataLakeEnvelopeRemover((partition, key) -> {
            throw new IllegalStateException("the object could not be deleted");
        }, monitor);

        assertThatThrownBy(() -> remover.remove(PARTITION, IdempotencyKey.of("payment-1"))).isInstanceOf(IllegalStateException.class);
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.UNHEALTHY);
    }
}
