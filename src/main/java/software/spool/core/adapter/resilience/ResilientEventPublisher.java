package software.spool.core.adapter.resilience;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.exception.EventBusEmitException;
import software.spool.core.model.Event;
import software.spool.core.port.bus.EventPublisher;
import software.spool.core.port.logging.Logger;
import software.spool.core.resilience.CircuitBreaker;
import software.spool.core.resilience.adapter.InMemoryCircuitBreakerStateStore;
import software.spool.core.resilience.control.RetryingExecutor;
import software.spool.core.resilience.control.TransitionEvaluator;
import software.spool.core.resilience.exception.CircuitBreakerOpenException;
import software.spool.core.resilience.model.CircuitBreakerPolicy;
import software.spool.core.resilience.model.CircuitBreakerStatus;
import software.spool.core.resilience.model.RetryPolicy;

import java.time.Duration;
import java.util.concurrent.Callable;

public class ResilientEventPublisher implements EventPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(ResilientEventPublisher.class);
    private final EventPublisher delegate;
    private final CircuitBreaker cb;
    private final RetryPolicy retryPolicy;

    public ResilientEventPublisher(EventPublisher delegate, CircuitBreaker cb, RetryPolicy retryPolicy) {
        this.delegate = delegate;
        this.cb = cb;
        this.retryPolicy = retryPolicy;
    }

    public static ResilientEventPublisher of(EventPublisher delegate) {
        CircuitBreaker cb = buildCircuitBreaker();
        RetryPolicy retryPolicy = RetryPolicy.fixedWithAbort(
                3,
                Duration.ofMillis(500),
                ex -> ex instanceof CircuitBreakerOpenException ||
                        cb.status() == CircuitBreakerStatus.HALF_OPEN
        );
        return new ResilientEventPublisher(delegate, cb, retryPolicy);
    }

    @Override
    public <E extends Event> void publish(E event) throws EventBusEmitException {
        execute(() -> { delegate.publish(event); return null; }, event);
    }

    private <T> T execute(Callable<T> action, Event event) {
        try {
            return new RetryingExecutor<>(retryPolicy, () -> cb.execute(action)).execute();
        } catch (CircuitBreakerOpenException ex) {
            LOG.warn("RESILIENCE - EventPublisher · {} » circuit OPEN, rejecting publish of {}",
                    cb.id(), event.eventType());
            throw new EventBusEmitException(event, "Circuit breaker is OPEN", ex);
        } catch (EventBusEmitException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new EventBusEmitException(event, ex.getMessage(), ex);
        }
    }

    private static CircuitBreaker buildCircuitBreaker() {
        CircuitBreakerPolicy policy = CircuitBreakerPolicy.countBased()
                .withFailureRateThreshold(0.50f)
                .withMinimumCalls(4)
                .withSlidingWindowSize(10)
                .withCooldown(Duration.ofSeconds(30))
                .withHalfOpenPermits(4)
                .build();
        return new CircuitBreaker(
                "event-publisher",
                policy,
                new TransitionEvaluator(),
                InMemoryCircuitBreakerStateStore.instance()
        );
    }
}
