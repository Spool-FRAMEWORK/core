package software.spool.core.circuitbreaker.adapter;

import software.spool.core.circuitbreaker.model.CircuitBreakerState;
import software.spool.core.circuitbreaker.port.CircuitBreakerStateStore;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryCircuitBreakerStateStore implements CircuitBreakerStateStore {
    private final ConcurrentMap<String, CircuitBreakerState> store = new ConcurrentHashMap<>();

    @Override
    public CircuitBreakerState load(String id) {
        return store.get(id);
    }

    @Override
    public boolean compareAndSet(long expectedVersion, CircuitBreakerState newState) {
        return store.compute(newState.snapshot().id(), (id, currentState) -> {
            if (currentState == null) {
                CircuitBreakerState result = null;
                if (expectedVersion == 0)
                    result = newState;
                return result;
            }

            if (currentState.snapshot().version() == expectedVersion) {
                return newState;
            } else {
                return currentState;
            }
        }) == newState;
    }
}