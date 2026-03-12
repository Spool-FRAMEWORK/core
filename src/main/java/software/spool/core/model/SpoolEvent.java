package software.spool.core.model;

/**
 * Marker interface for Spool-internal events.
 *
 * <p>
 * All domain events emitted by Spool modules (Crawler, Publisher, Ingester)
 * implement this interface to distinguish framework events from user-defined
 * ones.
 * </p>
 */
public interface SpoolEvent extends Event {

}
