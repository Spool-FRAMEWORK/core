package software.spool.core.port.decorator;

import software.spool.core.exception.InboxReadException;
import software.spool.core.exception.SpoolException;
import software.spool.core.model.vo.InboxItem;
import software.spool.core.model.InboxItemStatus;
import software.spool.core.port.inbox.InboxReader;

import java.util.stream.Stream;

/**
 * Decorator for {@link InboxReader} that normalises unchecked exceptions
 * into typed {@link InboxReadException} instances.
 */
public class SafeInboxReader implements InboxReader {
    private final InboxReader reader;

    public SafeInboxReader(InboxReader reader) {
        this.reader = reader;
    }

    @Override
    public Stream<InboxItem> findByStatus(InboxItemStatus status) {
        try {
            return reader.findByStatus(status);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new InboxReadException(e.getMessage(), e);
        }
    }

    public static SafeInboxReader of(InboxReader reader) {
        return new SafeInboxReader(reader);
    }
}
