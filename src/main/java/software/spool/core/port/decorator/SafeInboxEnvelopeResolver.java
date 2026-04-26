package software.spool.core.port.decorator;

import software.spool.core.exception.InboxReadException;
import software.spool.core.exception.SpoolException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;
import software.spool.core.model.vo.IdempotencyKey;
import software.spool.core.port.inbox.InboxEnvelopeResolver;
import software.spool.core.port.inbox.InboxStatusQuery;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SafeInboxEnvelopeResolver implements InboxEnvelopeResolver {
    private final InboxEnvelopeResolver inbox;

    public SafeInboxEnvelopeResolver(InboxEnvelopeResolver inbox) {
        this.inbox = inbox;
    }

    @Override
    public Optional<Envelope> findById(IdempotencyKey idempotencyKey) throws InboxReadException {
        try {
            return inbox.findById(idempotencyKey);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new InboxReadException(e.getMessage(), e);
        }
    }

    @Override
    public Collection<Envelope> findByIds(Collection<IdempotencyKey> idempotencyKeys) throws InboxReadException {
        try {
            return inbox.findByIds(idempotencyKeys);
        } catch (SpoolException e) {
            throw e;
        } catch (Exception e) {
            throw new InboxReadException(e.getMessage(), e);
        }
    }

    public static SafeInboxEnvelopeResolver of(InboxEnvelopeResolver reader) {
        return new SafeInboxEnvelopeResolver(reader);
    }
}
