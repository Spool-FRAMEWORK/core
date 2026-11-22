package software.spool.core.adapter.health;

import software.spool.core.exception.EnvelopeHeaderStoreException;
import software.spool.core.port.health.PieceMonitor;
import software.spool.core.resilience.exception.CircuitBreakerOpenException;

import java.util.function.Supplier;

/**
 * Makes a call to a store of envelope headers that its {@link PieceMonitor} watches. A call the circuit of the piece
 * rejects fails as the store fails, with an {@link EnvelopeHeaderStoreException}, so whoever uses the store handles
 * it as any other failure of the store.
 */
final class EnvelopeHeaderStoreCalls {
    private EnvelopeHeaderStoreCalls() {
    }

    static <T> T call(Supplier<T> watchedCall, String whatIsAsked) {
        try {
            return watchedCall.get();
        } catch (CircuitBreakerOpenException rejected) {
            throw new EnvelopeHeaderStoreException(whatIsAsked + " was not asked of the store of envelope headers: "
                    + "its circuit is open", rejected);
        }
    }
}
