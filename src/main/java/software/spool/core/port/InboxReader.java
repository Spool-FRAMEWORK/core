package software.spool.core.port;

import software.spool.core.exception.InboxReadException;
import software.spool.core.model.InboxItemStatus;
import software.spool.core.model.InboxItem;

import java.util.stream.Stream;

public interface InboxReader {
    Stream<InboxItem> findByStatus(InboxItemStatus status) throws InboxReadException;
}
