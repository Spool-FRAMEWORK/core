package software.spool.core.port.inbox;

import software.spool.core.exception.InboxReadException;
import software.spool.core.model.vo.InboxItem;
import software.spool.core.model.InboxItemStatus;

import java.util.stream.Stream;

/**
 * Port for reading inbox items filtered by their current status.
 */
public interface InboxReader {
    /**
     * Returns all inbox items matching the given status.
     *
     * @param status the status to filter by; must not be {@code null}
     * @return a stream of matching inbox items
     * @throws InboxReadException if the query fails
     */
    Stream<InboxItem> findByStatus(InboxItemStatus status) throws InboxReadException;
}
