package software.spool.core.utils;

import java.time.Duration;

public class PollingPolicy {
    private final Duration interval;
    public static final PollingPolicy ONCE = new PollingPolicy(Duration.ZERO) {
        @Override public boolean isOneShot() { return true; }
    };

    public PollingPolicy(Duration interval) {
        this.interval = interval;
    }

    public static PollingPolicy every(Duration interval) {
        return new PollingPolicy(interval);
    }

    public boolean shouldPoll(Duration elapsed) {
        return elapsed.compareTo(interval) > 0;
    }

    public boolean isOneShot() { return false; }
}
