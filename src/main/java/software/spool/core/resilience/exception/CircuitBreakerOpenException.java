package software.spool.core.resilience.exception;

import software.spool.core.exception.SpoolException;

import java.time.Instant;
import java.util.Optional;

/**
 * Thrown when a call is rejected because the circuit of what it calls is open. It is a {@link SpoolException} so the
 * decorators that wrap the failures of a port let it through as it is.
 */
public class CircuitBreakerOpenException extends SpoolException {
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
