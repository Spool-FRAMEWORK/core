package software.spool.core.resilience.model;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Optional;

public record CircuitBreakerState(
        CircuitBreakerSnapshot snapshot,
        CircuitBreakerStatus status,
        Optional<Instant> openUntil
) {
    public boolean isOpenExpired(Instant now) {
        return openUntil.isPresent() && !now.isBefore(openUntil.get());
    }

    public CircuitBreakerState trip(Instant until) {
        return new CircuitBreakerState(snapshot, CircuitBreakerStatus.OPEN, Optional.of(until));
    }

    public CircuitBreakerState reset(Instant now) {
        CircuitBreakerSnapshot fresh = new CircuitBreakerSnapshot(
                snapshot.id(), 0, 0, 0, now, new ArrayDeque<>(), snapshot.version() + 1
        );
        return new CircuitBreakerState(fresh, CircuitBreakerStatus.CLOSED, Optional.empty());
    }

    public CircuitBreakerState toHalfOpen(Instant now) {
        CircuitBreakerSnapshot trials = new CircuitBreakerSnapshot(
                snapshot.id(), 0, 0, 0, now, new ArrayDeque<>(), snapshot.version() + 1
        );
        return new CircuitBreakerState(trials, CircuitBreakerStatus.HALF_OPEN, openUntil);
    }
}