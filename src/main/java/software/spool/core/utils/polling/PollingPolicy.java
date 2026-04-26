package software.spool.core.utils.polling;

import java.time.Duration;

public class PollingPolicy {
    private final Duration interval;
    private final boolean oneShot;
    public static final PollingPolicy ONCE = new PollingPolicy(Duration.ZERO, true);

    public PollingPolicy(Duration interval) {
        this.interval = interval;
        this.oneShot = interval.isZero();
    }

    private PollingPolicy(Duration interval, boolean oneShot) {
        this.interval = interval;
        this.oneShot = oneShot;
    }

    public static PollingPolicy every(Duration interval) {
        return new PollingPolicy(interval);
    }

    public boolean shouldPoll(Duration elapsed) {
        return elapsed.compareTo(interval) > 0;
    }

    public boolean isOneShot() { return oneShot; }
}
