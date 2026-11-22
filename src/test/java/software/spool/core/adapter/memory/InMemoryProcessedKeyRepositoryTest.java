package software.spool.core.adapter.memory;

import org.junit.jupiter.api.Test;
import software.spool.core.port.idempotency.ProcessedKeyRepository;
import software.spool.core.port.idempotency.ProcessedKeyRepositoryContract;

import java.time.Clock;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryProcessedKeyRepositoryTest extends ProcessedKeyRepositoryContract {

    @Override
    protected ProcessedKeyRepository repository(Duration window, Clock clock) {
        return new InMemoryProcessedKeyRepository(window, clock);
    }

    @Test
    void new_windowOfZero_isRejected() {
        assertThatThrownBy(() -> InMemoryProcessedKeyRepository.remembering(Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The processed keys window must be longer than zero, got PT0S");
    }
}
