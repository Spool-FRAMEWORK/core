package software.spool.core.circuitbreaker;

import java.time.Duration;
import java.time.Instant;

public record CircuitBreakerSnapshot(
        String id,
        int failures,
        int successes,
        int halfOpenAttempts,
        Instant windowStartedAt,
        long version
) {
    public boolean isWindowExpired(Instant now, Duration samplingWindow) {
        return now.isAfter(windowStartedAt.plus(samplingWindow));
    }

    public CircuitBreakerSnapshot withFailure(Instant now, Duration samplingWindow) {
        if (isWindowExpired(now, samplingWindow)) {
            return new CircuitBreakerSnapshot(id, 1, 0, halfOpenAttempts, now, version + 1);
        }
        return new CircuitBreakerSnapshot(id, failures + 1, successes, halfOpenAttempts, windowStartedAt, version + 1);
    }

    public CircuitBreakerSnapshot withSuccess(Instant now, Duration samplingWindow) {
        if (isWindowExpired(now, samplingWindow)) {
            return new CircuitBreakerSnapshot(id, 0, 1, halfOpenAttempts, now, version + 1);
        }
        return new CircuitBreakerSnapshot(id, failures, successes + 1, halfOpenAttempts, windowStartedAt, version + 1);
    }

    public CircuitBreakerSnapshot withHalfOpenAttempt() {
        return new CircuitBreakerSnapshot(id, failures, successes, halfOpenAttempts + 1, windowStartedAt, version + 1);
    }

    public int totalCalls() {
        return failures + successes;
    }
}