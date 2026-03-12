package software.spool.core.adapter;

import software.spool.core.port.Subscription;

/**
 * In-memory {@link Subscription} implementation that executes a
 * {@link Runnable} on cancellation.
 *
 * <p>
 * Cancellation is idempotent — calling {@link #cancel()} more than once
 * has no additional effect.
 * </p>
 */
public class InMemorySubscription implements Subscription {

    private final Runnable onCancel;
    private volatile boolean active = true;

    public InMemorySubscription(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    @Override
    public void cancel() {
        if (active) {
            active = false;
            onCancel.run();
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
