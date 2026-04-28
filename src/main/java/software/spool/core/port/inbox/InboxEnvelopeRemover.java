package software.spool.core.port.inbox;

import software.spool.core.model.vo.Envelope;

public interface InboxEnvelopeRemover {
    Envelope remove(Envelope envelope);
}
