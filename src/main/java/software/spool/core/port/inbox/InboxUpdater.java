package software.spool.core.port.inbox;

import software.spool.core.exception.InboxUpdateException;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.model.InboxItemStatus;
import software.spool.core.model.vo.InboxItem;

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
     * @return the updated {@link InboxItem}
     * @throws InboxUpdateException if the update fails
     */
    InboxItem update(IdempotencyKey idempotencyKey, InboxItemStatus status) throws InboxUpdateException;
}
