package software.spool.core.port.inbox;

import software.spool.core.exception.InboxReadException;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public interface InboxEnvelopeResolver {
    Optional<Envelope> findById(IdempotencyKey key) throws InboxReadException;
    Stream<Envelope> findByIds(Collection<IdempotencyKey> keys) throws InboxReadException;
}
