package software.spool.core.circuitbreaker;

import java.time.Instant;
import java.util.concurrent.Callable;

public record CircuitBreaker(
        String id,
        CircuitBreakerPolicy policy,
        TransitionEvaluator evaluator,
        CircuitBreakerStateStore store
) {
    public <T> T execute(Callable<T> action) throws Exception {
        acquirePermit();
        try {
            T result = action.call();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    public CircuitBreakerStatus status() {
        return store.load(id).status();
    }

    public void acquirePermit() {
        Instant now = Instant.now();
        CircuitBreakerState current = store.load(id);
        CircuitBreakerState evaluated = evaluator.evaluateOnInitialize(current, policy, now);

        if (evaluated.status() != current.status()) {
            store.compareAndSet(current.snapshot().version(), evaluated);
        }

        if (evaluated.status() == CircuitBreakerStatus.OPEN) {
            throw new CircuitBreakerOpenException(id);
        }

        if (evaluated.status() == CircuitBreakerStatus.HALF_OPEN
                && evaluated.snapshot().halfOpenAttempts() >= policy.halfOpenPermits()) {
            throw new CircuitBreakerOpenException(id);
        }
    }

    public void onSuccess() {
        Instant now = Instant.now();
        CircuitBreakerState current = store.load(id);
        CircuitBreakerState next = evaluator.evaluateOnSuccess(current, policy, now);
        store.compareAndSet(current.snapshot().version(), next);
    }

    public void onFailure() {
        Instant now = Instant.now();
        CircuitBreakerState current = store.load(id);
        CircuitBreakerState next = evaluator.evaluateOnFailure(current, policy, now);
        store.compareAndSet(current.snapshot().version(), next);
    }
}