package software.spool.core.adapter.health;

import org.junit.jupiter.api.Test;
import software.spool.core.exception.EnvelopeHeaderStoreException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.EnvelopeHeader;
import software.spool.core.model.vo.EventMetadata;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.vo.MediaType;
import software.spool.core.model.vo.PartitionKey;
import software.spool.core.port.datalake.EnvelopeHeaderReader;
import software.spool.core.port.datalake.EnvelopeHeaderRemover;
import software.spool.core.port.datalake.EnvelopeHeaderWriter;
import software.spool.core.port.health.HealthStatus;
import software.spool.core.port.health.PieceCheckPolicy;
import software.spool.core.port.health.PieceMonitor;
import software.spool.core.resilience.model.CircuitBreakerPolicy;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonitoredEnvelopeHeaderStoreTest {
    private static final PartitionKey PARTITION = new PartitionKey("source=orders");
    private static final IdempotencyKey KEY = IdempotencyKey.of("order-1");
    private static final EnvelopeHeader HEADER = new EnvelopeHeader(KEY, new EventMetadata(),
            MediaType.of("application/json"), EnvelopeStatus.CAPTURED, 0, Instant.EPOCH, Instant.EPOCH);
    private static final int CALLS_THAT_OPEN_THE_CIRCUIT = 2;

    private final PieceMonitor monitor = PieceMonitor.of("headers", "POSTGRESQL", Optional.empty(),
            PieceCheckPolicy.defaults(), CircuitBreakerPolicy.timeBased()
                    .withFailureRateThreshold(1.0f)
                    .withMinimumCalls(CALLS_THAT_OPEN_THE_CIRCUIT)
                    .withSamplingWindow(Duration.ofMinutes(1))
                    .withCooldown(Duration.ofMinutes(1))
                    .withHalfOpenPermits(1)
                    .build());

    @Test
    void writer_headerTheStoreKeeps_leavesThePieceHealthy() {
        new MonitoredEnvelopeHeaderWriter((partition, header) -> {
        }, monitor).write(PARTITION, HEADER);

        assertThat(monitor.health().status()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    void writer_whenTheStoreFails_marksThePieceUnhealthyAndFailsTheSameWay() {
        EnvelopeHeaderWriter writer = new MonitoredEnvelopeHeaderWriter(failingWriter(), monitor);

        assertThatThrownBy(() -> writer.write(PARTITION, HEADER)).isInstanceOf(EnvelopeHeaderStoreException.class)
                .hasMessage("the database refused the row");
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.UNHEALTHY);
        assertThat(monitor.health().reason()).hasValueSatisfying(reason -> assertThat(reason).contains("the database refused the row"));
    }

    @Test
    void writer_whileTheCircuitIsOpen_failsAsTheStoreDoesWithoutReachingIt() {
        AtomicInteger reached = new AtomicInteger();
        EnvelopeHeaderWriter writer = new MonitoredEnvelopeHeaderWriter((partition, header) -> {
            reached.incrementAndGet();
            throw new EnvelopeHeaderStoreException("the database refused the row", new IllegalStateException());
        }, monitor);
        openTheCircuit(() -> writer.write(PARTITION, HEADER));
        reached.set(0);

        assertThatThrownBy(() -> writer.write(PARTITION, HEADER)).isInstanceOf(EnvelopeHeaderStoreException.class)
                .hasMessageContaining("order-1").hasMessageContaining("circuit");
        assertThat(reached).hasValue(0);
    }

    @Test
    void reader_whenTheStoreFails_marksThePieceUnhealthy() {
        EnvelopeHeaderReader reader = new MonitoredEnvelopeHeaderReader(failingReader(), monitor);

        assertThatThrownBy(() -> reader.find(KEY)).isInstanceOf(EnvelopeHeaderStoreException.class);
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.UNHEALTHY);
    }

    @Test
    void reader_headersOfAPartition_areGivenAsTheStoreGivesThem() {
        EnvelopeHeaderReader reader = new MonitoredEnvelopeHeaderReader(storeWith(HEADER), monitor);

        assertThat(reader.findIn(PARTITION)).containsExactly(HEADER);
        assertThat(reader.find(KEY)).contains(HEADER);
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    void reader_whileTheCircuitIsOpen_failsAsTheStoreDoes() {
        EnvelopeHeaderReader reader = new MonitoredEnvelopeHeaderReader(failingReader(), monitor);
        openTheCircuit(() -> reader.find(KEY));

        assertThatThrownBy(() -> reader.findIn(PARTITION)).isInstanceOf(EnvelopeHeaderStoreException.class)
                .hasMessageContaining("source=orders").hasMessageContaining("circuit");
        assertThatThrownBy(() -> reader.find(KEY)).isInstanceOf(EnvelopeHeaderStoreException.class)
                .hasMessageContaining("order-1").hasMessageContaining("circuit");
    }

    @Test
    void remover_whenTheStoreFails_marksThePieceUnhealthy() {
        EnvelopeHeaderRemover remover = new MonitoredEnvelopeHeaderRemover(failingRemover(), monitor);

        assertThatThrownBy(() -> remover.remove(PARTITION, KEY)).isInstanceOf(EnvelopeHeaderStoreException.class);
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.UNHEALTHY);
    }

    @Test
    void remover_whileTheCircuitIsOpen_failsAsTheStoreDoes() {
        EnvelopeHeaderRemover remover = new MonitoredEnvelopeHeaderRemover(failingRemover(), monitor);
        openTheCircuit(() -> remover.remove(PARTITION, KEY));

        assertThatThrownBy(() -> remover.remove(PARTITION, KEY)).isInstanceOf(EnvelopeHeaderStoreException.class)
                .hasMessageContaining("order-1").hasMessageContaining("circuit");
    }

    private void openTheCircuit(Runnable failingCall) {
        for (int call = 0; call < CALLS_THAT_OPEN_THE_CIRCUIT; call++) {
            assertThatThrownBy(failingCall::run).isInstanceOf(EnvelopeHeaderStoreException.class);
        }
        assertThat(monitor.health().reason()).hasValueSatisfying(reason -> assertThat(reason).contains("circuit open"));
    }

    private static EnvelopeHeaderWriter failingWriter() {
        return (partition, header) -> {
            throw new EnvelopeHeaderStoreException("the database refused the row", new IllegalStateException());
        };
    }

    private static EnvelopeHeaderRemover failingRemover() {
        return (partition, key) -> {
            throw new EnvelopeHeaderStoreException("the database refused the delete", new IllegalStateException());
        };
    }

    private static EnvelopeHeaderReader failingReader() {
        return new EnvelopeHeaderReader() {
            @Override
            public Optional<EnvelopeHeader> find(IdempotencyKey idempotencyKey) {
                throw new EnvelopeHeaderStoreException("the database did not answer", new IllegalStateException());
            }

            @Override
            public Stream<EnvelopeHeader> findIn(PartitionKey partition) {
                throw new EnvelopeHeaderStoreException("the database did not answer", new IllegalStateException());
            }
        };
    }

    private static EnvelopeHeaderReader storeWith(EnvelopeHeader header) {
        return new EnvelopeHeaderReader() {
            @Override
            public Optional<EnvelopeHeader> find(IdempotencyKey idempotencyKey) {
                return Optional.of(header);
            }

            @Override
            public Stream<EnvelopeHeader> findIn(PartitionKey partition) {
                return Stream.of(header);
            }
        };
    }
}
