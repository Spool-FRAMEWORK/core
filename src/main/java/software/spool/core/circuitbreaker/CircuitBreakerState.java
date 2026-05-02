package software.spool.core.circuitbreaker;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public record CircuitBreakerState(
        CircuitBreakerSnapshot snapshot,
        CircuitBreakerStatus status,
        Optional<Instant> openedAt
) {
    public boolean isExpired(Duration cooldown) {
        return openedAt().isPresent() && Instant.now().isAfter(openedAt.get());
    }

    public CircuitBreakerState trip() {
        return new CircuitBreakerState(snapshot, status, Optional.of(Instant.now()));
    }

    public CircuitBreakerState reset() {
        return new CircuitBreakerState(snapshot, CircuitBreakerStatus.CLOSED, Optional.empty());
    }

    public CircuitBreakerState toHalfOpen() {
        return new CircuitBreakerState(snapshot, CircuitBreakerStatus.HALF_OPEN, Optional.of(Instant.now()));
    }
}
