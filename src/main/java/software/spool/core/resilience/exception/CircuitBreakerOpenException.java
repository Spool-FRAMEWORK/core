package software.spool.core.resilience.exception;

import java.time.Instant;
import java.util.Optional;

public class CircuitBreakerOpenException extends RuntimeException {
    private final Optional<Instant> openUntil;

    public CircuitBreakerOpenException(String id) {
        this(id, Optional.empty());
    }

    public CircuitBreakerOpenException(String id, Optional<Instant> openUntil) {
        super("Circuit breaker '" + id + "' is OPEN — calls are rejected");
        this.openUntil = openUntil;
    }

    /** The time the circuit lets calls through again, when it says one. */
    public Optional<Instant> openUntil() {
        return openUntil;
    }
}
