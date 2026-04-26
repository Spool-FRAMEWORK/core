package software.spool.core.port.decorator;

import software.spool.core.exception.InboxReadException;
import software.spool.core.exception.SpoolException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.port.inbox.InboxStatusQuery;

import java.util.Collection;

public class SafeInboxStatusQuery implements InboxStatusQuery {
    private final InboxStatusQuery inbox;

    public SafeInboxStatusQuery(InboxStatusQuery inbox) {
        this.inbox = inbox;
    }

    @Override
    public Collection<Envelope> findByStatus(EnvelopeStatus status) {
        try {
            return inbox.findByStatus(status);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new InboxReadException(e.getMessage(), e);
        }
    }

    public static SafeInboxStatusQuery of(InboxStatusQuery reader) {
        return new SafeInboxStatusQuery(reader);
    }
}
