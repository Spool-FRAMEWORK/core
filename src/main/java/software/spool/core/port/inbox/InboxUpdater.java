package software.spool.core.port.inbox;

import software.spool.core.exception.InboxUpdateException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;

public interface InboxUpdater {
    Envelope update(Envelope envelope) throws InboxUpdateException;
    Envelope update(IdempotencyKey idempotencyKey, EnvelopeStatus status) throws InboxUpdateException;
}
