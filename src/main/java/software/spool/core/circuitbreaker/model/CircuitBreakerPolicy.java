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
    public static Builder timeBased() {
        return new Builder(SlidingWindowType.TIME_BASED);
    }

    public static Builder countBased() {
        return new Builder(SlidingWindowType.TIME_BASED);
    }

    public boolean shouldTrip(CircuitBreakerSnapshot snapshot) {
        int total = snapshot.totalCalls();
        if (total < minimumCalls) return false;
        float rate = (float) snapshot.failures() / total;
        return rate >= failureRateThreshold;
    }

    public static class Builder {
        private float failureRateThreshold;
        private int minimumCalls;
        private Duration samplingWindow;
        private int slidingWindowSize;
        private final SlidingWindowType windowType;
        private Duration cooldown;
        private int halfOpenPermits;

        private Builder(SlidingWindowType windowType) {
            this.windowType = windowType;
        }

        public Builder withFailureRateThreshold(float failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
            return this;
        }

        public Builder withMinimumCalls(int minimumCalls) {
            this.minimumCalls = minimumCalls;
            return this;
        }

        public Builder withSamplingWindow(Duration samplingWindow) {
            this.samplingWindow = samplingWindow;
            return this;
        }

        public Builder withSlidingWindowSize(int slidingWindowSize) {
            this.slidingWindowSize = slidingWindowSize;
            return this;
        }

        public Builder withCooldown(Duration cooldown) {
            this.cooldown = cooldown;
            return this;
        }

        public Builder withHalfOpenPermits(int halfOpenPermits) {
            this.halfOpenPermits = halfOpenPermits;
            return this;
        }

        public CircuitBreakerPolicy build() {
            return new CircuitBreakerPolicy(
                    failureRateThreshold, minimumCalls,
                    samplingWindow, slidingWindowSize,
                    windowType, cooldown, halfOpenPermits
            );
        }
    }
}