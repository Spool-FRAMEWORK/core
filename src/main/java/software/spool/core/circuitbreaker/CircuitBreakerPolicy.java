package software.spool.core.circuitbreaker;

import java.time.Duration;

public record CircuitBreakerPolicy(
        float failureRateThreshold,
        int minimumCalls,
        Duration samplingWindow,
        Duration cooldown,
        int halfOpenPermits
) {
    public boolean shouldTrip(CircuitBreakerSnapshot snapshot) {
        int total = snapshot.totalCalls();
        if (total < minimumCalls) return false;
        float rate = (float) snapshot.failures() / total;
        return rate >= failureRateThreshold;
    }
}