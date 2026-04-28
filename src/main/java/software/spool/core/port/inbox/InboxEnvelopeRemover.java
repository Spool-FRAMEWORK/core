package software.spool.core.port.inbox;

import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;

import java.util.Collection;
import java.util.List;

public interface InboxEnvelopeRemover {
    default Envelope remove(IdempotencyKey idempotencyKey) {
        return remove(List.of(idempotencyKey)).stream().findFirst().orElse(null);
    };
    Collection<Envelope> remove(Collection<IdempotencyKey> idempotencyKeys);
}
