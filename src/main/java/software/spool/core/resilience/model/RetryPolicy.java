package software.spool.core.resilience.model;

import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;

public record RetryPolicy(
        int maximumAttempts,
        List<Duration> delays,
        Predicate<Throwable> abortOn
) {
    public boolean canRetry(int attempt, Throwable exception) {
        if (abortOn.test(exception)) return false;
        return attempt < maximumAttempts;
    }

    public Duration getDelayOf(int attempt) {
        if (delays.isEmpty()) return Duration.ZERO;
        int index = Math.min(attempt, delays.size() - 1);
        return delays.get(index);
    }

    public static RetryPolicy fixed(int maximumAttempts, Duration delay) {
        return new RetryPolicy(
                maximumAttempts,
                List.of(delay),
                ex -> false
        );
    }

    public static RetryPolicy exponential(int maximumAttempts, Duration base) {
        List<Duration> delays = new java.util.ArrayList<>();
        for (int i = 0; i < maximumAttempts; i++) {
            delays.add(base.multipliedBy((long) Math.pow(2, i)));
        }
        return new RetryPolicy(maximumAttempts, List.copyOf(delays), ex -> false);
    }


    public static RetryPolicy fixedWithAbort(int maximumAttempts, Duration delay, Predicate<Throwable> abortOn) {
        return new RetryPolicy(maximumAttempts, List.of(delay), abortOn);
    }
}