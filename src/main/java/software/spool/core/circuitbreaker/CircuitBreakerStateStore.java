package software.spool.core.circuitbreaker;

public interface CircuitBreakerStateStore {
    CircuitBreakerState load(String id);
    boolean compareAndSet(long expectedVersion, CircuitBreakerState state);
}
