package software.spool.core.circuitbreaker;

import java.time.Instant;

public record CircuitBreakerSnapshot(
        String id,
        int failures,
        int successes,
        int halfOpenAttempts,
        Instant windowStartedAt,
        long version
) {
    public boolean isWindowExpire() {
        return Instant.now().isAfter(windowStartedAt);
    }

    public CircuitBreakerSnapshot withFailures(int failures) {
        return new CircuitBreakerSnapshot(id, failures, successes, halfOpenAttempts, windowStartedAt, version);
    }

    public CircuitBreakerSnapshot withSuccesses(int successes) {
        return new CircuitBreakerSnapshot(id, failures, successes, halfOpenAttempts, windowStartedAt, version);
    }

    public CircuitBreakerSnapshot withHalfOpenAttempts(int halfOpenAttempts) {
        return new CircuitBreakerSnapshot(id, failures, successes, halfOpenAttempts, windowStartedAt, version);
    }
}
