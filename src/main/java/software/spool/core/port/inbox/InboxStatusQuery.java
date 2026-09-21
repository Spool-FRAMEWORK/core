package software.spool.core.port.inbox;

import software.spool.core.exception.InboxReadException;
import software.spool.core.model.EnvelopeStatus;
import software.spool.core.model.vo.Envelope;

import java.time.Instant;
import java.util.Collection;

public interface InboxStatusQuery {
    Collection<Envelope> findByStatus(EnvelopeStatus status) throws InboxReadException;

    /**
     * Finds the envelopes in a status that last changed before a moment.
     *
     * <p>By default it filters what {@link #findByStatus} returns. An inbox that can tell how old each envelope is
     * without reading it should override this, so that a large folder is not read in full to find a few old
     * envelopes.</p>
     *
     * @param status the status to look in
     * @param limit  only envelopes that last changed before this moment are returned
     * @return the envelopes, in no particular order
     * @throws InboxReadException if the inbox cannot be read
     */
    default Collection<Envelope> findByStatusModifiedBefore(EnvelopeStatus status, Instant limit) throws InboxReadException {
        return findByStatus(status).stream()
                .filter(envelope -> envelope.lastModifiedAt().isBefore(limit))
                .toList();
    }
}
