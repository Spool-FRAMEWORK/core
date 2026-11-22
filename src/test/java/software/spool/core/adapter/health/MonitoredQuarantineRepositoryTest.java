package software.spool.core.adapter.health;

import org.junit.jupiter.api.Test;
import software.spool.core.exception.QuarantineConflictException;
import software.spool.core.exception.QuarantineRepositoryException;
import software.spool.core.model.quarantine.QuarantinePage;
import software.spool.core.model.quarantine.QuarantinePageRequest;
import software.spool.core.model.quarantine.QuarantineStatus;
import software.spool.core.model.quarantine.QuarantinedRecord;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.health.HealthStatus;
import software.spool.core.port.health.PieceCheckPolicy;
import software.spool.core.port.health.PieceMonitor;
import software.spool.core.port.quarantine.QuarantineRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonitoredQuarantineRepositoryTest {
    private static final IdempotencyKey KEY = IdempotencyKey.of("payment-1");

    private final PieceMonitor monitor = PieceMonitor.of("quarantine", "FILE_SYSTEM", Optional.empty(), PieceCheckPolicy.defaults());

    @Test
    void find_whenTheQuarantineFails_marksThePieceUnhealthy() {
        QuarantineRepository repository = new MonitoredQuarantineRepository(repositoryThatThrows(() ->
                new QuarantineRepositoryException("the quarantine folder cannot be read", new IllegalStateException())), monitor);

        assertThatThrownBy(() -> repository.find(KEY)).isInstanceOf(QuarantineRepositoryException.class);
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.UNHEALTHY);
    }

    @Test
    void resolve_whenTheRecordWasResolvedMeanwhile_leavesThePieceHealthy() {
        QuarantineRepository repository = new MonitoredQuarantineRepository(repositoryThatThrows(() ->
                new QuarantineConflictException(KEY, "the record was resolved meanwhile")), monitor);

        assertThatThrownBy(() -> repository.resolve(null)).isInstanceOf(QuarantineConflictException.class);
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.HEALTHY);
    }

    private static QuarantineRepository repositoryThatThrows(Supplier<RuntimeException> failure) {
        return new QuarantineRepository() {
            @Override
            public void save(QuarantinedRecord quarantinedRecord) {
                throw failure.get();
            }

            @Override
            public void resolve(QuarantinedRecord resolvedRecord) {
                throw failure.get();
            }

            @Override
            public Optional<QuarantinedRecord> find(IdempotencyKey idempotencyKey) {
                throw failure.get();
            }

            @Override
            public Collection<QuarantinedRecord> findByStatus(QuarantineStatus status) {
                throw failure.get();
            }

            @Override
            public QuarantinePage findByStatus(QuarantinePageRequest request) {
                throw failure.get();
            }

            @Override
            public Collection<QuarantinedRecord> findAll() {
                throw failure.get();
            }

            @Override
            public void delete(IdempotencyKey idempotencyKey) {
                throw failure.get();
            }
        };
    }
}
