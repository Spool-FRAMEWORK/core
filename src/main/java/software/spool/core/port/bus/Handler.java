package software.spool.core.port.bus;

import software.spool.core.exception.SpoolException;

/**
 * Functional interface for event handlers in the Spool framework.
 *
 * <p>
 * Implementations process a single object (typically an
 * {@link software.spool.core.model.Event})
 * and may throw a {@link SpoolException} on failure.
 * </p>
 *
 * @param <T> the type of object this handler processes
 */
public interface Handler<T> {
    void handle(T object) throws SpoolException;
}
