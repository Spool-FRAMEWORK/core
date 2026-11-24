package software.spool.core.port.idempotency;

import org.junit.jupiter.api.Test;
import software.spool.core.adapter.memory.InMemoryProcessedKeyRepository;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.testing.MovingClock;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProcessedKeyWindowTest {
    private static final Duration WINDOW = Duration.ofMinutes(10);
    private static final IdempotencyKey KEY = IdempotencyKey.of("order-1");
    private static final IdempotencyKey OTHER_KEY = IdempotencyKey.of("order-2");

    private final MovingClock clock = new MovingClock(Instant.parse("2026-11-22T10:00:00Z"));
    private final ProcessedKeyWindow window = new ProcessedKeyWindow(new InMemoryProcessedKeyRepository(), WINDOW, clock);

    @Test
    void remembers_aKeyProcessedWithinTheWindow() {
        window.remember(KEY);
        clock.advance(WINDOW.minusSeconds(1));

        assertThat(window.remembers(KEY)).isTrue();
        assertThat(window.remembers(OTHER_KEY)).isFalse();
    }

    @Test
    void remembers_aKeyProcessedAWindowAgo_isFalse() {
        window.remember(KEY);
        clock.advance(WINDOW.plusSeconds(1));

        assertThat(window.remembers(KEY)).isFalse();
    }

    @Test
    void remember_aKeyAgain_startsItsWindowAgain() {
        window.remember(KEY);
        clock.advance(WINDOW.minusMinutes(1));
        window.remember(KEY);
        clock.advance(Duration.ofMinutes(2));

        assertThat(window.remembers(KEY)).isTrue();
    }

    @Test
    void remember_aKeyProcessedEarlier_countsTheWindowFromThatMoment() {
        window.remember(KEY, clock.instant().minus(WINDOW).minusSeconds(1));

        assertThat(window.remembers(KEY)).isFalse();
    }

    @Test
    void new_windowOfZero_isRejected() {
        assertThatThrownBy(() -> new ProcessedKeyWindow(new InMemoryProcessedKeyRepository(), Duration.ZERO, clock))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("The processed keys window must be longer than zero, got PT0S");
    }
}
