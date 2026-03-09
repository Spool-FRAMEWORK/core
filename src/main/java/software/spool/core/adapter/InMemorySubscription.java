package software.spool.core.adapter;

import software.spool.core.port.Subscription;

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
