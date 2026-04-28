package software.spool.core.port.inbox;

import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;

public interface InboxEnvelopeRemover {
    Envelope remove(IdempotencyKey idempotencyKey);
}
