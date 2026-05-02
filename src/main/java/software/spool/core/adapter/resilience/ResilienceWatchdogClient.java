package software.spool.core.adapter.resilience;

import software.spool.core.adapter.logging.LoggerFactory;
import software.spool.core.adapter.watchdog.HttpWatchdogClient;
import software.spool.core.adapter.watchdog.WatchdogUnavailableException;
import software.spool.core.resilience.CircuitBreaker;
import software.spool.core.resilience.adapter.InMemoryCircuitBreakerStateStore;
import software.spool.core.resilience.control.RetryingExecutor;
import software.spool.core.resilience.control.TransitionEvaluator;
import software.spool.core.resilience.exception.CircuitBreakerOpenException;
import software.spool.core.resilience.model.CircuitBreakerPolicy;
import software.spool.core.resilience.model.CircuitBreakerStatus;
import software.spool.core.resilience.model.RetryPolicy;
import software.spool.core.model.watchdog.ModuleIdentity;
import software.spool.core.model.watchdog.ModuleState;
import software.spool.core.model.watchdog.ModuleStatus;
import software.spool.core.port.logging.Logger;
import software.spool.core.port.watchdog.WatchdogClient;

import java.time.Duration;
import java.util.Collection;

public class ResilienceWatchdogClient implements WatchdogClient {
    private static final Logger LOG = LoggerFactory.getLogger(ResilienceWatchdogClient.class);
    private final WatchdogClient delegate;
    private final CircuitBreaker cb;
    private final RetryPolicy retryPolicy;

    public ResilienceWatchdogClient(WatchdogClient delegate) {
        this.delegate = delegate;
        this.cb = buildCircuitBreaker();
        this.retryPolicy = RetryPolicy.fixedWithAbort(
                3,
                Duration.ofMillis(500),
                ex -> ex instanceof CircuitBreakerOpenException ||
                        cb.status() == CircuitBreakerStatus.HALF_OPEN
        );
    }

    public static ResilienceWatchdogClient defaults() {
        return new ResilienceWatchdogClient(new HttpWatchdogClient("http://localhost:8090"));
    }

    @Override
    public Collection<ModuleState> query() {
        return execute(delegate::query);
    }

    @Override
    public void beat(ModuleIdentity identity, ModuleStatus status) {
        execute(() -> {
            delegate.beat(identity, status);
            return null;
        });
    }

    private <T> T execute(java.util.concurrent.Callable<T> action) {
        try {
            return new RetryingExecutor<>(retryPolicy, () -> cb.execute(action)).execute();
        } catch (CircuitBreakerOpenException ex) {
            LOG.warn("RESILIENCE - Watchdog · {} » skipping beat, circuit is OPEN", cb.id());
            return null;
        } catch (Exception ex) {
            throw new WatchdogUnavailableException("Watchdog unreachable", ex);
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
                "watchdog-client",
                policy,
                new TransitionEvaluator(),
                InMemoryCircuitBreakerStateStore.instance()
        );
    }
}