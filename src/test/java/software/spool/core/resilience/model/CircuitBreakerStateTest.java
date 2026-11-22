package software.spool.core.resilience.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CircuitBreakerStateTest {

    private CircuitBreakerState closedState() {
        CircuitBreakerSnapshot snap = new CircuitBreakerSnapshot("test", 0, 0, 0, Instant.now(), new ArrayDeque<>(), 0L);
        return new CircuitBreakerState(snap, CircuitBreakerStatus.CLOSED, Optional.empty());
    }

    private CircuitBreakerState openState(Instant openUntil) {
        CircuitBreakerSnapshot snap = new CircuitBreakerSnapshot("test", 5, 0, 0, Instant.now(), new ArrayDeque<>(), 0L);
        return new CircuitBreakerState(snap, CircuitBreakerStatus.OPEN, Optional.of(openUntil));
    }

    @Test
    void trip_closedState_transitionsToOpen() {
        Instant until = Instant.now().plusSeconds(30);
        CircuitBreakerState tripped = closedState().trip(until);
        assertThat(tripped.status()).isEqualTo(CircuitBreakerStatus.OPEN);
        assertThat(tripped.openUntil()).contains(until);
    }

    @Test
    void reset_openState_transitionsToClosed() {
        CircuitBreakerState reset = openState(Instant.now().minusSeconds(60)).reset(Instant.now());
        assertThat(reset.status()).isEqualTo(CircuitBreakerStatus.CLOSED);
        assertThat(reset.openUntil()).isEmpty();
    }

    @Test
    void toHalfOpen_openExpired_transitionsToHalfOpen() {
        CircuitBreakerState halfOpen = openState(Instant.now().minusSeconds(60)).toHalfOpen(Instant.now());
        assertThat(halfOpen.status()).isEqualTo(CircuitBreakerStatus.HALF_OPEN);
        assertThat(halfOpen.snapshot().totalCalls()).isZero();
    }

    @Test
    void isOpenExpired_beforeTheTimeItOpensUntil_returnsFalse() {
        Instant now = Instant.now();
        assertThat(openState(now.plusSeconds(20)).isOpenExpired(now)).isFalse();
    }

    @Test
    void isOpenExpired_fromTheTimeItOpensUntil_returnsTrue() {
        Instant now = Instant.now();
        assertThat(openState(now).isOpenExpired(now)).isTrue();
    }
}
