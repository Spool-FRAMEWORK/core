package software.spool.core.circuitbreaker.port;

import software.spool.core.circuitbreaker.model.CircuitBreakerState;

public interface CircuitBreakerStateStore {
    CircuitBreakerState load(String id);
    boolean compareAndSet(long expectedVersion, CircuitBreakerState state);
}
