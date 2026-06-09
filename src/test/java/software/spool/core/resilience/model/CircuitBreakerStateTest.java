package software.spool.core.resilience.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CircuitBreakerStateTest {

    private CircuitBreakerState closedState() {
        CircuitBreakerSnapshot snap = new CircuitBreakerSnapshot("test", 0, 0, 0, Instant.now(), new ArrayDeque<>(), 0L);
        return new CircuitBreakerState(snap, CircuitBreakerStatus.CLOSED, Optional.empty());
    }

    private CircuitBreakerState openState(Instant openedAt) {
        CircuitBreakerSnapshot snap = new CircuitBreakerSnapshot("test", 5, 0, 0, Instant.now(), new ArrayDeque<>(), 0L);
        return new CircuitBreakerState(snap, CircuitBreakerStatus.OPEN, Optional.of(openedAt));
    }

    @Test
    void trip_closedState_transitionsToOpen() {
        CircuitBreakerState tripped = closedState().trip(Instant.now());
        assertThat(tripped.status()).isEqualTo(CircuitBreakerStatus.OPEN);
        assertThat(tripped.openedAt()).isPresent();
    }

    @Test
    void reset_openState_transitionsToClosed() {
        CircuitBreakerState reset = openState(Instant.now().minusSeconds(60)).reset(Instant.now());
        assertThat(reset.status()).isEqualTo(CircuitBreakerStatus.CLOSED);
        assertThat(reset.openedAt()).isEmpty();
    }

    @Test
    void toHalfOpen_openExpired_transitionsToHalfOpen() {
        CircuitBreakerState halfOpen = openState(Instant.now().minusSeconds(60)).toHalfOpen();
        assertThat(halfOpen.status()).isEqualTo(CircuitBreakerStatus.HALF_OPEN);
    }

    @Test
    void isOpenExpired_withinCooldown_returnsFalse() {
        Instant now = Instant.now();
        assertThat(openState(now.minusSeconds(10)).isOpenExpired(now, Duration.ofSeconds(30))).isFalse();
    }

    @Test
    void isOpenExpired_pastCooldown_returnsTrue() {
        Instant now = Instant.now();
        assertThat(openState(now.minusSeconds(60)).isOpenExpired(now, Duration.ofSeconds(30))).isTrue();
    }
}
