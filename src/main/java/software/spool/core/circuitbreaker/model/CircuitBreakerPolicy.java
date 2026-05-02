package software.spool.core.circuitbreaker.model;

import java.time.Duration;

public record CircuitBreakerPolicy(
        float failureRateThreshold,
        int minimumCalls,
        Duration samplingWindow,
        int slidingWindowSize,
        SlidingWindowType windowType,
        Duration cooldown,
        int halfOpenPermits
) {
    public static CircuitBreakerPolicy timeBased(
            float failureRateThreshold,
            int minimumCalls,
            Duration samplingWindow,
            Duration cooldown,
            int halfOpenPermits
    ) {
        return new CircuitBreakerPolicy(
                failureRateThreshold, minimumCalls,
                samplingWindow, 0,
                SlidingWindowType.TIME_BASED,
                cooldown, halfOpenPermits
        );
    }

    public static CircuitBreakerPolicy countBased(
            float failureRateThreshold,
            int minimumCalls,
            int slidingWindowSize,
            Duration cooldown,
            int halfOpenPermits
    ) {
        return new CircuitBreakerPolicy(
                failureRateThreshold, minimumCalls,
                Duration.ZERO, slidingWindowSize,
                SlidingWindowType.COUNT_BASED,
                cooldown, halfOpenPermits
        );
    }

    public boolean shouldTrip(CircuitBreakerSnapshot snapshot) {
        int total = snapshot.totalCalls();
        if (total < minimumCalls) return false;
        float rate = (float) snapshot.failures() / total;
        return rate >= failureRateThreshold;
    }
}