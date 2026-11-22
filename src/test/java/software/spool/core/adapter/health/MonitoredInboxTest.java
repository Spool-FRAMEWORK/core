package software.spool.core.adapter.health;

import org.junit.jupiter.api.Test;
import software.spool.core.exception.InboxReadException;
import software.spool.core.exception.InboxUpdateException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.health.HealthStatus;
import software.spool.core.port.health.PieceCheckPolicy;
import software.spool.core.port.health.PieceMonitor;
import software.spool.core.port.inbox.InboxEnvelopeRemover;
import software.spool.core.port.inbox.InboxReader;
import software.spool.core.port.inbox.InboxUpdater;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonitoredInboxTest {
    private final PieceMonitor monitor = PieceMonitor.of("inbox", "FILE_SYSTEM", Optional.empty(), PieceCheckPolicy.defaults());

    @Test
    void reader_whenTheInboxFails_passesTheFailureOnAndMarksThePieceUnhealthy() {
        InboxReader reader = new MonitoredInboxReader(readerThatFails(), monitor);

        assertThatThrownBy(() -> reader.findByStatus(EnvelopeStatus.CAPTURED)).isInstanceOf(InboxReadException.class);

        assertThat(monitor.health().status()).isEqualTo(HealthStatus.UNHEALTHY);
        assertThat(monitor.health().reason()).hasValueSatisfying(reason -> assertThat(reason).contains("the inbox folder cannot be read"));
    }

    @Test
    void reader_whenTheInboxAnswers_givesWhatItFound() {
        InboxReader reader = new MonitoredInboxReader(emptyReader(), monitor);

        assertThat(reader.findByStatus(EnvelopeStatus.CAPTURED)).isEmpty();
        assertThat(reader.findById(IdempotencyKey.of("payment-1"))).isEmpty();
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.HEALTHY);
    }

    @Test
    void updater_whenTheInboxFails_marksThePieceUnhealthy() {
        InboxUpdater updater = new MonitoredInboxUpdater((keys, status) -> {
            throw new InboxUpdateException(keys, "the inbox folder cannot be written", new IllegalStateException());
        }, monitor);

        assertThatThrownBy(() -> updater.update(List.of(IdempotencyKey.of("payment-1")), EnvelopeStatus.PERSISTED))
                .isInstanceOf(InboxUpdateException.class);
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.UNHEALTHY);
    }

    @Test
    void remover_whenTheInboxAnswers_givesWhatItRemoved() {
        InboxEnvelopeRemover remover = new MonitoredInboxEnvelopeRemover(keys -> List.of(), monitor);

        assertThat(remover.remove(List.of(IdempotencyKey.of("payment-1")))).isEmpty();
        assertThat(monitor.health().status()).isEqualTo(HealthStatus.HEALTHY);
    }

    private static InboxReader readerThatFails() {
        return new InboxReader() {
            @Override
            public Collection<Envelope> findByStatus(EnvelopeStatus status) {
                throw new InboxReadException("the inbox folder cannot be read");
            }

            @Override
            public Optional<Envelope> findById(IdempotencyKey idempotencyKey) {
                throw new InboxReadException("the inbox folder cannot be read");
            }

            @Override
            public Collection<Envelope> findByIds(Collection<IdempotencyKey> idempotencyKeys) {
                throw new InboxReadException("the inbox folder cannot be read");
            }
        };
    }

    private static InboxReader emptyReader() {
        return new InboxReader() {
            @Override
            public Collection<Envelope> findByStatus(EnvelopeStatus status) {
                return List.of();
            }

            @Override
            public Optional<Envelope> findById(IdempotencyKey idempotencyKey) {
                return Optional.empty();
            }

            @Override
            public Collection<Envelope> findByIds(Collection<IdempotencyKey> idempotencyKeys) {
                return List.of();
            }
        };
    }
}
