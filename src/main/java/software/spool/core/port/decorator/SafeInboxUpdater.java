package software.spool.core.port.decorator;

import software.spool.core.exception.InboxUpdateException;
import software.spool.core.exception.SpoolException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.inbox.InboxUpdater;

import java.util.Collection;

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
    public Collection<Envelope> update(Collection<IdempotencyKey> idempotencyKeys, EnvelopeStatus status) throws InboxUpdateException {
        try {
            return updater.update(idempotencyKeys, status);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new InboxUpdateException(idempotencyKeys, e.getMessage(), e);
        }
    }

    public static SafeInboxUpdater of(InboxUpdater updater) {
        return new SafeInboxUpdater(updater);
    }
}
