package software.spool.core.port.inbox;

import software.spool.core.exception.InboxReadException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;

import java.util.Collection;

public interface InboxStatusQuery {
    Collection<Envelope> findByStatus(EnvelopeStatus status) throws InboxReadException;
}