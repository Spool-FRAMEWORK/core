package software.spool.core.circuitbreaker.adapter;

import software.spool.core.circuitbreaker.model.CircuitBreakerState;
import software.spool.core.circuitbreaker.model.CircuitBreakerSnapshot;
import software.spool.core.circuitbreaker.model.CircuitBreakerStatus;
import software.spool.core.circuitbreaker.port.CircuitBreakerStateStore;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemoryCircuitBreakerStateStore implements CircuitBreakerStateStore {
    private final ConcurrentMap<String, CircuitBreakerState> store = new ConcurrentHashMap<>();

    @Override
    public CircuitBreakerState load(String id) {
        return store.computeIfAbsent(id, this::initialState);
    }

    @Override
    public boolean compareAndSet(long expectedVersion, CircuitBreakerState newState) {
        store.computeIfAbsent(newState.snapshot().id(), this::initialState);
        return store.compute(newState.snapshot().id(), (key, currentState) -> {
            if (currentState == null || currentState.snapshot().version() == expectedVersion) {
                return newState;
            }
            return currentState;
        }) == newState;
    }

    private CircuitBreakerState initialState(String id) {
        CircuitBreakerSnapshot snap = new CircuitBreakerSnapshot(
                id, 0, 0, 0, Instant.now(), new ArrayDeque<>(), 0L
        );
        return new CircuitBreakerState(snap, CircuitBreakerStatus.CLOSED, Optional.empty());
    }
}