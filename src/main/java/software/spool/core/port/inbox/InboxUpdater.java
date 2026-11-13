package software.spool.core.port.inbox;

import software.spool.core.exception.InboxUpdateException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;

import java.util.Collection;
import java.util.List;

/**
 * Changes the status of envelopes already in the inbox.
 *
 * <p>
 * A key that is not in the inbox is skipped without error: the rest of the batch is still
 * updated, and the returned collection only holds the envelopes that were updated.
 * </p>
 */
public interface InboxUpdater {
    default Envelope update(Envelope envelope) throws InboxUpdateException {
        return update(envelope.idempotencyKey(), envelope.status());
    };
    default Envelope update(IdempotencyKey idempotencyKey, EnvelopeStatus status) throws InboxUpdateException {
        return update(List.of(idempotencyKey), status).stream().findFirst().orElse(null);
    };
    Collection<Envelope> update(Collection<IdempotencyKey> idempotencyKeys, EnvelopeStatus status) throws InboxUpdateException;
}
