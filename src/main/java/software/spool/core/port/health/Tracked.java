package software.spool.core.port.health;

public class Tracked<T> implements HealthProbe {
    private final T component;
    private final String name;
    private final long degradedThresholdMs;
    private volatile HealthCheck current;

    private Tracked(T component, String name, long degradedThresholdMs) {
        this.component = component;
        this.name = name;
        this.degradedThresholdMs = degradedThresholdMs;
        this.current = HealthCheck.healthy(name);
    }

    public static <T> Tracked<T> of(T component, String name) {
        return new Tracked<>(component, name, 0);
    }

    public static <T> Tracked<T> of(T component, String name, long degradedThresholdMs) {
        return new Tracked<>(component, name, degradedThresholdMs);
    }

    public <R> R call(CheckedFunction<T, R> fn) throws Exception {
        long start = System.nanoTime();
        try {
            R result = fn.apply(component);
            current = resolveSuccess(start);
            return result;
        } catch (Exception e) {
            current = HealthCheck.unhealthy(name, e.getMessage());
            throw e;
        }
    }

    public void run(CheckedConsumer<T> fn) throws Exception {
        long start = System.nanoTime();
        try {
            fn.accept(component);
            current = resolveSuccess(start);
        } catch (Exception e) {
            current = HealthCheck.unhealthy(name, e.getMessage());
            throw e;
        }
    }

    public T get() {
        return component;
    }

    public void recordSuccess() {
        current = HealthCheck.healthy(name);
    }

    public void recordFailure(String reason) {
        current = HealthCheck.unhealthy(name, reason);
    }

    @Override
    public HealthCheck probe() {
        return current;
    }

    private HealthCheck resolveSuccess(long startNanos) {
        long elapsed = (System.nanoTime() - startNanos) / 1_000_000;
        if (degradedThresholdMs > 0 && elapsed >= degradedThresholdMs)
            return HealthCheck.degraded(name, "response time: " + elapsed + "ms");
        return HealthCheck.healthy(name);
    }

    @FunctionalInterface
    public interface CheckedFunction<T, R> {
        R apply(T t) throws Exception;
    }

    @FunctionalInterface
    public interface CheckedConsumer<T> {
        void accept(T t) throws Exception;
    }
}
