package software.spool.core.port.health;

public class PolledHealthProbe implements HealthProbe {
    private final String name;
    private final long degradedThresholdMs;
    private final CheckedRunnable check;

    private PolledHealthProbe(String name, long degradedThresholdMs, CheckedRunnable check) {
        this.name = name;
        this.degradedThresholdMs = degradedThresholdMs;
        this.check = check;
    }

    public static PolledHealthProbe of(String name, CheckedRunnable check) {
        return new PolledHealthProbe(name, 0, check);
    }

    public static PolledHealthProbe of(String name, long degradedThresholdMs, CheckedRunnable check) {
        return new PolledHealthProbe(name, degradedThresholdMs, check);
    }

    @Override
    public HealthCheck probe() {
        long start = System.nanoTime();
        try {
            check.run();
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            if (degradedThresholdMs > 0 && elapsed >= degradedThresholdMs)
                return HealthCheck.degraded(name, "response time: " + elapsed + "ms");
            return HealthCheck.healthy(name);
        } catch (Exception e) {
            return HealthCheck.unhealthy(name, e.getMessage());
        }
    }

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }
}
