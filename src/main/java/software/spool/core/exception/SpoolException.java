package software.spool.core.exception;

import software.spool.core.utils.routing.ErrorRouter;

/**
 * Abstract base class for all typed runtime exceptions in the Spool framework.
 *
 * <p>
 * Each Spool port operation has a corresponding concrete subclass so that
 * the {@link ErrorRouter} can dispatch by type.
 * </p>
 */
public abstract class SpoolException extends RuntimeException {
    protected SpoolException(String message) {
        super(message);
    }

    protected SpoolException(String message, Throwable cause) {
        super(message, cause);
    }
}
