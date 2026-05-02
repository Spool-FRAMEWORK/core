package software.spool.core.circuitbreaker.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Optional;

public record CircuitBreakerState(
        CircuitBreakerSnapshot snapshot,
        CircuitBreakerStatus status,
        Optional<Instant> openedAt
) {
    public boolean isOpenExpired(Instant now, Duration cooldown) {
        return openedAt.isPresent() && now.isAfter(openedAt.get().plus(cooldown));
    }

    public CircuitBreakerState trip(Instant now) {
        return new CircuitBreakerState(snapshot, CircuitBreakerStatus.OPEN, Optional.of(now));
    }

    public CircuitBreakerState reset(Instant now) {
        CircuitBreakerSnapshot fresh = new CircuitBreakerSnapshot(
                snapshot.id(), 0, 0, 0, now, new ArrayDeque<>(), snapshot.version() + 1
        );
        return new CircuitBreakerState(fresh, CircuitBreakerStatus.CLOSED, Optional.empty());
    }

    public CircuitBreakerState toHalfOpen() {
        return new CircuitBreakerState(snapshot, CircuitBreakerStatus.HALF_OPEN, openedAt);
    }
}