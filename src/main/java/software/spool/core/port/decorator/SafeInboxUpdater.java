package software.spool.core.port.decorator;

import software.spool.core.exception.InboxUpdateException;
import software.spool.core.exception.SpoolException;
import software.spool.core.model.vo.Envelope;
import software.spool.core.port.inbox.InboxUpdater;

/**
 * Decorator that wraps an {@link InboxUpdater} and normalises any
 * unchecked exception into an {@link InboxUpdateException}.
 *
 * <p>
 * Exceptions that are already a {@link SpoolException} are rethrown as-is.
 * </p>
 */
public class SafeInboxUpdater implements InboxUpdater {
    private final InboxUpdater updater;

    public SafeInboxUpdater(InboxUpdater updater) {
        this.updater = updater;
    }

    @Override
    public Envelope update(Envelope envelope) {
        try {
            return updater.update(envelope);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new InboxUpdateException(envelope.idempotencyKey(), e.getMessage(), e);
        }
    }

    public static SafeInboxUpdater of(InboxUpdater updater) {
        return new SafeInboxUpdater(updater);
    }
}
