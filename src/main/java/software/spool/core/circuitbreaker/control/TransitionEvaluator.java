package software.spool.core.circuitbreaker.control;

import software.spool.core.circuitbreaker.model.CircuitBreakerPolicy;
import software.spool.core.circuitbreaker.model.CircuitBreakerSnapshot;
import software.spool.core.circuitbreaker.model.CircuitBreakerState;
import software.spool.core.circuitbreaker.model.CircuitBreakerStatus;

import java.time.Instant;

public class TransitionEvaluator {

    public CircuitBreakerState evaluateOnInitialize(CircuitBreakerState state,
                                                    CircuitBreakerPolicy policy,
                                                    Instant now) {
        if (state.status() == CircuitBreakerStatus.OPEN && state.isOpenExpired(now, policy.cooldown())) {
            return state.toHalfOpen();
        }
        return state;
    }

    public CircuitBreakerState evaluateOnSuccess(CircuitBreakerState state,
                                                 CircuitBreakerPolicy policy,
                                                 Instant now) {
        CircuitBreakerSnapshot updated = state.snapshot().withSuccess(now, policy);
        CircuitBreakerState next = new CircuitBreakerState(updated, state.status(), state.openedAt());

        if (state.status() == CircuitBreakerStatus.HALF_OPEN) {
            if (updated.successes() >= policy.halfOpenPermits()) {
                return next.reset(now);
            }
            return next;
        }
        return next;
    }

    public CircuitBreakerState evaluateOnFailure(CircuitBreakerState state,
                                                 CircuitBreakerPolicy policy,
                                                 Instant now) {
        CircuitBreakerSnapshot updated = state.snapshot().withFailure(now, policy);
        CircuitBreakerState next = new CircuitBreakerState(updated, state.status(), state.openedAt());

        if (state.status() == CircuitBreakerStatus.HALF_OPEN) {
            return next.trip(now);
        }

        if (state.status() == CircuitBreakerStatus.CLOSED && policy.shouldTrip(updated)) {
            return next.trip(now);
        }

        return next;
    }
}