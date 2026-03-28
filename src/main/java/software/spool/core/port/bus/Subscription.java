package software.spool.core.port.bus;

/**
 * Handle returned after subscribing to events on an {@link EventBusListener}.
 *
 * <p>
 * Call {@link #cancel()} to unsubscribe. The {@link #NULL} constant
 * represents a no-op subscription for uninitialised state.
 * </p>
 */
public interface Subscription {
    Subscription NULL = new Subscription() {
        @Override
        public void cancel() {
        }

        @Override
        public boolean isActive() {
            return false;
        }
    };

    void cancel();

    boolean isActive();
}
