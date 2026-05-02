package software.spool.core.resilience;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.resilience.control.TransitionEvaluator;
import software.spool.core.resilience.exception.CircuitBreakerOpenException;
import software.spool.core.resilience.model.CircuitBreakerPolicy;
import software.spool.core.resilience.model.CircuitBreakerState;
import software.spool.core.resilience.model.CircuitBreakerStatus;
import software.spool.core.resilience.port.CircuitBreakerStateStore;
import software.spool.core.port.logging.Logger;

import java.time.Instant;
import java.util.concurrent.Callable;

public record CircuitBreaker(
        String id,
        CircuitBreakerPolicy policy,
        TransitionEvaluator evaluator,
        CircuitBreakerStateStore store
) {
    private static final Logger LOG = LoggerFactory.getLogger(CircuitBreaker.class);

    public <T> T execute(Callable<T> action) throws Exception {
        acquirePermit();
        try {
            T result = action.call();
            onSuccess();
            return result;
        } catch (CircuitBreakerOpenException e) {
            throw e;
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
            LOG.info("RESILIENCE - CB · {} » {} → {}", id, current.status(), evaluated.status());
        }

        if (evaluated.status() == CircuitBreakerStatus.OPEN) {
            LOG.warn("RESILIENCE - CB · {} » REJECTED  [failures={} total={} rate={}%]",
                    id,
                    evaluated.snapshot().failures(),
                    evaluated.snapshot().totalCalls(),
                    failureRate(evaluated));
            throw new CircuitBreakerOpenException(id);
        }

        if (evaluated.status() == CircuitBreakerStatus.HALF_OPEN
                && evaluated.snapshot().halfOpenAttempts() >= policy.halfOpenPermits()) {
            LOG.warn("RESILIENCE - CB · {} » REJECTED (half-open slots full)  [probes={}/{}]",
                    id,
                    evaluated.snapshot().halfOpenAttempts(),
                    policy.halfOpenPermits());
            throw new CircuitBreakerOpenException(id);
        }
    }

    public void onSuccess() {
        Instant now = Instant.now();
        CircuitBreakerState current = store.load(id);
        CircuitBreakerState next = evaluator.evaluateOnSuccess(current, policy, now);
        store.compareAndSet(current.snapshot().version(), next);

        if (next.status() != current.status()) {
            LOG.info("RESILIENCE - CB · {} » {} → {}  [recovered after {} failures]",
                    id, current.status(), next.status(), current.snapshot().failures());
        } else {
            LOG.debug("RESILIENCE - CB · {} » OK  [ok={} fail={} rate={}%]",
                    id,
                    next.snapshot().successes(),
                    next.snapshot().failures(),
                    failureRate(next));
        }
    }

    public void onFailure() {
        Instant now = Instant.now();
        CircuitBreakerState current = store.load(id);
        CircuitBreakerState next = evaluator.evaluateOnFailure(current, policy, now);
        store.compareAndSet(current.snapshot().version(), next);

        if (next.status() == CircuitBreakerStatus.OPEN && current.status() != CircuitBreakerStatus.OPEN) {
            LOG.error("RESILIENCE - CB · {} » OPEN  [rate={}% >= threshold={}% after {} calls]",
                    id,
                    failureRate(next),
                    (int) (policy.failureRateThreshold() * 100),
                    next.snapshot().totalCalls());
        } else {
            LOG.debug("RESILIENCE - CB · {} » FAIL  [ok={} fail={} rate={}%]",
                    id,
                    next.snapshot().successes(),
                    next.snapshot().failures(),
                    failureRate(next));
        }
    }

    private int failureRate(CircuitBreakerState state) {
        int total = state.snapshot().totalCalls();
        if (total == 0) return 0;
        return (int) ((float) state.snapshot().failures() / total * 100);
    }
}