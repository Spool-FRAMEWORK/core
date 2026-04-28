package software.spool.core.port.inbox;

import software.spool.core.exception.InboxUpdateException;
import software.spool.core.model.vo.Envelope;

public interface InboxUpdater {
    Envelope update(Envelope envelope) throws InboxUpdateException;
}
