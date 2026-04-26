package software.spool.core.port.inbox;

import software.spool.core.exception.InboxReadException;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;

import java.util.Collection;
import java.util.Optional;

public interface InboxEnvelopeResolver {
    Optional<Envelope> findById(IdempotencyKey idempotencyKey) throws InboxReadException;
    Collection<Envelope> findByIds(Collection<IdempotencyKey> idempotencyKeys) throws InboxReadException;
}
