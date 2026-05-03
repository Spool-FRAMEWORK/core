package software.spool.core.resilience.port;

import software.spool.core.resilience.model.CircuitBreakerState;

public interface CircuitBreakerStateStore {
    CircuitBreakerState load(String id);
    boolean compareAndSet(long expectedVersion, CircuitBreakerState state);
}
