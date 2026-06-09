package software.spool.core.resilience.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CircuitBreakerPolicyTest {

    private CircuitBreakerSnapshot snapshotWithCounts(int failures, int successes) {
        return new CircuitBreakerSnapshot("test", failures, successes, 0, Instant.now(), new ArrayDeque<>(), 0L);
    }

    @Test
    void shouldTrip_belowThreshold_returnsFalse() {
        CircuitBreakerPolicy policy = CircuitBreakerPolicy.countBased()
                .withFailureRateThreshold(0.5f)
                .withMinimumCalls(4)
                .withSlidingWindowSize(10)
                .withCooldown(Duration.ofSeconds(30))
                .withHalfOpenPermits(2)
                .build();

        assertThat(policy.shouldTrip(snapshotWithCounts(1, 3))).isFalse();
    }

    @Test
    void shouldTrip_aboveThreshold_returnsTrue() {
        CircuitBreakerPolicy policy = CircuitBreakerPolicy.countBased()
                .withFailureRateThreshold(0.5f)
                .withMinimumCalls(4)
                .withSlidingWindowSize(10)
                .withCooldown(Duration.ofSeconds(30))
                .withHalfOpenPermits(2)
                .build();

        assertThat(policy.shouldTrip(snapshotWithCounts(3, 1))).isTrue();
    }

    @Test
    void timeBased_builder_setsWindowType() {
        CircuitBreakerPolicy policy = CircuitBreakerPolicy.timeBased()
                .withFailureRateThreshold(0.5f)
                .withMinimumCalls(4)
                .withSamplingWindow(Duration.ofSeconds(60))
                .withCooldown(Duration.ofSeconds(30))
                .withHalfOpenPermits(2)
                .build();

        assertThat(policy.windowType()).isEqualTo(SlidingWindowType.TIME_BASED);
    }

    @Test
    void countBased_builder_setsWindowType() {
        CircuitBreakerPolicy policy = CircuitBreakerPolicy.countBased()
                .withFailureRateThreshold(0.5f)
                .withMinimumCalls(4)
                .withSlidingWindowSize(10)
                .withCooldown(Duration.ofSeconds(30))
                .withHalfOpenPermits(2)
                .build();

        assertThat(policy.windowType()).isEqualTo(SlidingWindowType.COUNT_BASED);
    }
}
