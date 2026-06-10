package software.spool.core.resilience;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.spool.core.resilience.adapter.InMemoryCircuitBreakerStateStore;
import software.spool.core.resilience.control.TransitionEvaluator;
import software.spool.core.resilience.exception.CircuitBreakerOpenException;
import software.spool.core.resilience.model.CircuitBreakerPolicy;
import software.spool.core.resilience.model.CircuitBreakerStatus;

import java.time.Duration;

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
}
