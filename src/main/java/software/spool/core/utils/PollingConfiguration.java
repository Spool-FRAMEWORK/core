package software.spool.core.utils;

import software.spool.core.port.PollingScheduler;

import java.time.Duration;

public record PollingConfiguration(PollingScheduler scheduler, PollingPolicy policy) {
    public static PollingConfiguration once() {
        return new PollingConfiguration(new ThreadedPollingScheduler(), PollingPolicy.ONCE);
    }

    public static PollingConfiguration every(Duration interval) {
        return new PollingConfiguration(new ThreadedPollingScheduler(), PollingPolicy.every(interval));
    }
}
