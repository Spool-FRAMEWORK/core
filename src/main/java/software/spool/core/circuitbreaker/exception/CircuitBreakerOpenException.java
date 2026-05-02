package software.spool.core.circuitbreaker.exception;

public class CircuitBreakerOpenException extends RuntimeException {
    public CircuitBreakerOpenException(String id) {
        super("Circuit breaker '" + id + "' is OPEN — calls are rejected");
    }
}