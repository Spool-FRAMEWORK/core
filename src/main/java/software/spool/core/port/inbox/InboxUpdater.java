package software.spool.core.port.inbox;

import software.spool.core.exception.InboxUpdateException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;

public interface InboxUpdater {
    default Envelope update(Envelope envelope) throws InboxUpdateException {
        return update(envelope.idempotencyKey(), envelope.status());
    };
    Envelope update(IdempotencyKey idempotencyKey, EnvelopeStatus status) throws InboxUpdateException;
}
