package software.spool.core.resilience;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.spool.core.resilience.adapter.InMemoryCircuitBreakerStateStore;
import software.spool.core.resilience.control.TransitionEvaluator;
import software.spool.core.resilience.exception.CircuitBreakerOpenException;
import software.spool.core.resilience.model.CircuitBreakerPolicy;
import software.spool.core.resilience.model.CircuitBreakerStatus;
import software.spool.core.testing.MovingClock;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CircuitBreakerTest {

    private CircuitBreaker cb;

    @BeforeEach
    void setUp() {
        CircuitBreakerPolicy policy = CircuitBreakerPolicy.countBased()
                .withFailureRateThreshold(0.5f)
                .withMinimumCalls(4)
                .withSlidingWindowSize(10)
                .withCooldown(Duration.ofSeconds(30))
                .withHalfOpenPermits(2)
                .build();
        cb = new CircuitBreaker("test-cb", policy, new TransitionEvaluator(), new InMemoryCircuitBreakerStateStore());
    }

    @Test
    void execute_successfulCallable_returnsResult() throws Exception {
        assertThat(cb.execute(() -> "ok")).isEqualTo("ok");
    }

    @Test
    void execute_failingCallable_recordsFailure() {
        assertThatThrownBy(() -> cb.execute(() -> { throw new RuntimeException("fail"); }))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("fail");
    }

    @Test
    void execute_failureRateExceeded_opensCircuit() {
        for (int i = 0; i < 4; i++) {
            try { cb.execute(() -> { throw new RuntimeException("fail"); }); } catch (Exception ignored) {}
        }
        assertThat(cb.status()).isEqualTo(CircuitBreakerStatus.OPEN);
    }

    @Test
    void execute_circuitOpen_throwsCallNotPermittedException() {
        for (int i = 0; i < 4; i++) {
            try { cb.execute(() -> { throw new RuntimeException("fail"); }); } catch (Exception ignored) {}
        }
        assertThatThrownBy(() -> cb.execute(() -> "should-not-run"))
                .isInstanceOf(CircuitBreakerOpenException.class);
    }

    @Test
    void execute_halfOpenSuccess_closesCircuit() throws Exception {
        CircuitBreakerPolicy policy = CircuitBreakerPolicy.countBased()
                .withFailureRateThreshold(0.5f)
                .withMinimumCalls(4)
                .withSlidingWindowSize(10)
                .withCooldown(Duration.ofMillis(1))
                .withHalfOpenPermits(2)
                .build();
        CircuitBreaker shortCb = new CircuitBreaker(
                "short-cooldown", policy, new TransitionEvaluator(), new InMemoryCircuitBreakerStateStore()
        );

        for (int i = 0; i < 4; i++) {
            try { shortCb.execute(() -> { throw new RuntimeException("fail"); }); } catch (Exception ignored) {}
        }
        assertThat(shortCb.status()).isEqualTo(CircuitBreakerStatus.OPEN);

        Thread.sleep(10);

        shortCb.execute(() -> "ok");
        shortCb.execute(() -> "ok");

        assertThat(shortCb.status()).isEqualTo(CircuitBreakerStatus.CLOSED);
    }

    @Test
    void holdOpenUntil_rejectsCallsUntilThatTimeAndThenLetsATrialCallThrough() throws Exception {
        MovingClock clock = new MovingClock(Instant.parse("2026-11-22T10:00:00Z"));
        CircuitBreaker breaker = onClock(clock, 1);

        breaker.holdOpenUntil(clock.instant().plusSeconds(3));
        clock.advance(Duration.ofSeconds(2));

        assertThatThrownBy(() -> breaker.execute(() -> "too soon")).isInstanceOf(CircuitBreakerOpenException.class);
        clock.advance(Duration.ofSeconds(1));
        assertThat(breaker.execute(() -> "trial")).isEqualTo("trial");
        assertThat(breaker.status()).isEqualTo(CircuitBreakerStatus.CLOSED);
    }

    @Test
    void openUntil_isTheTimeTheCircuitLetsCallsThroughAgainWhileItIsOpen() {
        MovingClock clock = new MovingClock(Instant.parse("2026-11-22T10:00:00Z"));
        CircuitBreaker breaker = onClock(clock, 1);
        assertThat(breaker.openUntil()).isEmpty();

        for (int i = 0; i < 4; i++) {
            try { breaker.execute(() -> { throw new RuntimeException("fail"); }); } catch (Exception ignored) {}
        }

        assertThat(breaker.openUntil()).contains(Instant.parse("2026-11-22T10:00:30Z"));
    }

    @Test
    void halfOpen_letsOnlyItsPermittedCallsThrough() {
        MovingClock clock = new MovingClock(Instant.parse("2026-11-22T10:00:00Z"));
        CircuitBreaker breaker = onClock(clock, 1);
        breaker.holdOpenUntil(clock.instant().plusSeconds(1));
        clock.advance(Duration.ofSeconds(1));

        breaker.acquirePermit();

        assertThatThrownBy(breaker::acquirePermit).isInstanceOf(CircuitBreakerOpenException.class);
    }

    @Test
    void halfOpen_closesOnlyAfterItsPermittedCallsSucceed_whateverSucceededBeforeItOpened() throws Exception {
        MovingClock clock = new MovingClock(Instant.parse("2026-11-22T10:00:00Z"));
        CircuitBreaker breaker = onClock(clock, 2);
        breaker.execute(() -> "ok");
        breaker.execute(() -> "ok");
        for (int i = 0; i < 2; i++) {
            try { breaker.execute(() -> { throw new RuntimeException("fail"); }); } catch (Exception ignored) {}
        }
        assertThat(breaker.status()).isEqualTo(CircuitBreakerStatus.OPEN);
        clock.advance(Duration.ofSeconds(30));

        breaker.execute(() -> "first trial");
        assertThat(breaker.status()).isEqualTo(CircuitBreakerStatus.HALF_OPEN);
        breaker.execute(() -> "second trial");
        assertThat(breaker.status()).isEqualTo(CircuitBreakerStatus.CLOSED);
    }

    @Test
    void defaults_haveNamedValues() {
        CircuitBreakerPolicy defaults = CircuitBreakerPolicy.defaults();

        assertThat(defaults.failureRateThreshold()).isEqualTo(CircuitBreakerPolicy.DEFAULT_FAILURE_RATE);
        assertThat(defaults.minimumCalls()).isEqualTo(CircuitBreakerPolicy.DEFAULT_MINIMUM_CALLS);
        assertThat(defaults.samplingWindow()).isEqualTo(CircuitBreakerPolicy.DEFAULT_WINDOW);
        assertThat(defaults.cooldown()).isEqualTo(CircuitBreakerPolicy.DEFAULT_OPEN_FOR);
        assertThat(defaults.halfOpenPermits()).isEqualTo(CircuitBreakerPolicy.DEFAULT_HALF_OPEN_CALLS);
    }

    @Test
    void execute_whileOpen_isRejectedSayingWhenTheCircuitLetsCallsThroughAgain() {
        MovingClock clock = new MovingClock(Instant.parse("2026-11-22T10:00:00Z"));
        CircuitBreaker breaker = onClock(clock, 1);
        breaker.holdOpenUntil(Instant.parse("2026-11-22T10:00:30Z"));

        assertThatThrownBy(() -> breaker.execute(() -> "too soon"))
                .isInstanceOfSatisfying(CircuitBreakerOpenException.class, open ->
                        assertThat(open.openUntil()).contains(Instant.parse("2026-11-22T10:00:30Z")));
    }

    @Test
    void acquirePermit_whenTheTrialCallsAreTaken_isRejectedWithoutATime() {
        MovingClock clock = new MovingClock(Instant.parse("2026-11-22T10:00:00Z"));
        CircuitBreaker breaker = onClock(clock, 1);
        breaker.holdOpenUntil(clock.instant().plusSeconds(1));
        clock.advance(Duration.ofSeconds(1));
        breaker.acquirePermit();

        assertThatThrownBy(breaker::acquirePermit)
                .isInstanceOfSatisfying(CircuitBreakerOpenException.class, open -> assertThat(open.openUntil()).isEmpty());
    }

    private static CircuitBreaker onClock(MovingClock clock, int halfOpenPermits) {
        CircuitBreakerPolicy policy = CircuitBreakerPolicy.countBased()
                .withFailureRateThreshold(0.5f)
                .withMinimumCalls(4)
                .withSlidingWindowSize(10)
                .withCooldown(Duration.ofSeconds(30))
                .withHalfOpenPermits(halfOpenPermits)
                .build();
        return new CircuitBreaker("clocked", policy, new TransitionEvaluator(), new InMemoryCircuitBreakerStateStore(), clock);
    }
}
