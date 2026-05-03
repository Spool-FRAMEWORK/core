package software.spool.core.resilience.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

public record CircuitBreakerSnapshot(
        String id,
        int failures,
        int successes,
        int halfOpenAttempts,
        Instant windowStartedAt,
        Deque<Boolean> callWindow,
        long version
) {
    public boolean isWindowExpired(Instant now, Duration samplingWindow) {
        return now.isAfter(windowStartedAt.plus(samplingWindow));
    }

    public CircuitBreakerSnapshot withFailure(Instant now, CircuitBreakerPolicy policy) {
        if (policy.windowType() == SlidingWindowType.COUNT_BASED) {
            return withFailureCount(policy.slidingWindowSize());
        }
        return withFailureTime(now, policy.samplingWindow());
    }

    public CircuitBreakerSnapshot withSuccess(Instant now, CircuitBreakerPolicy policy) {
        if (policy.windowType() == SlidingWindowType.COUNT_BASED) {
            return withSuccessCount(policy.slidingWindowSize());
        }
        return withSuccessTime(now, policy.samplingWindow());
    }

    private CircuitBreakerSnapshot withFailureTime(Instant now, Duration samplingWindow) {
        if (isWindowExpired(now, samplingWindow)) {
            return new CircuitBreakerSnapshot(id, 1, 0, halfOpenAttempts, now, new ArrayDeque<>(), version + 1);
        }
        return new CircuitBreakerSnapshot(id, failures + 1, successes, halfOpenAttempts, windowStartedAt, callWindow, version + 1);
    }

    private CircuitBreakerSnapshot withSuccessTime(Instant now, Duration samplingWindow) {
        if (isWindowExpired(now, samplingWindow)) {
            return new CircuitBreakerSnapshot(id, 0, 1, halfOpenAttempts, now, new ArrayDeque<>(), version + 1);
        }
        return new CircuitBreakerSnapshot(id, failures, successes + 1, halfOpenAttempts, windowStartedAt, callWindow, version + 1);
    }

    private CircuitBreakerSnapshot withFailureCount(int windowSize) {
        Deque<Boolean> window = new ArrayDeque<>(callWindow);
        window.addLast(false);
        int evictedSuccesses = 0, evictedFailures = 0;
        if (window.size() > windowSize) {
            boolean evicted = window.removeFirst();
            if (evicted) evictedSuccesses = 1;
            else evictedFailures = 1;
        }
        return new CircuitBreakerSnapshot(
                id,
                failures + 1 - evictedFailures,
                successes - evictedSuccesses,
                halfOpenAttempts,
                windowStartedAt,
                window,
                version + 1
        );
    }

    private CircuitBreakerSnapshot withSuccessCount(int windowSize) {
        Deque<Boolean> window = new ArrayDeque<>(callWindow);
        window.addLast(true);
        int evictedSuccesses = 0, evictedFailures = 0;
        if (window.size() > windowSize) {
            boolean evicted = window.removeFirst();
            if (evicted) evictedSuccesses = 1;
            else evictedFailures = 1;
        }
        return new CircuitBreakerSnapshot(
                id,
                failures - evictedFailures,
                successes + 1 - evictedSuccesses,
                halfOpenAttempts,
                windowStartedAt,
                window,
                version + 1
        );
    }

    public CircuitBreakerSnapshot withHalfOpenAttempt() {
        return new CircuitBreakerSnapshot(id, failures, successes, halfOpenAttempts + 1, windowStartedAt, callWindow, version + 1);
    }

    public int totalCalls() {
        return failures + successes;
    }
}