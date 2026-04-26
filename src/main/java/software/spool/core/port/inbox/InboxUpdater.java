package software.spool.core.port.inbox;

import software.spool.core.exception.InboxUpdateException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;

/**
 * Output port for updating the status of inbox items.
 *
 * @see software.spool.core.port.decorator.SafeInboxUpdater
 */
public interface InboxUpdater {
    /**
     * Updates the status of the inbox item identified by the given key.
     *
     * @param idempotencyKey the key identifying the inbox item
     * @param status         the new status to set
     * @throws InboxUpdateException if the update fails
     */
    Envelope update(IdempotencyKey idempotencyKey, EnvelopeStatus status) throws InboxUpdateException;
}
