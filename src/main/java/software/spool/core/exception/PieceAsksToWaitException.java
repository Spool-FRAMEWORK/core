package software.spool.core.exception;

import java.time.Instant;

/**
 * Thrown when a piece answers that it cannot take calls until a given time, such as a source that answers HTTP 429
 * with a Retry-After header. The monitor of the piece opens its circuit until then.
 */
public class PieceAsksToWaitException extends SpoolException {
    private final String piece;
    private final Instant until;

    public PieceAsksToWaitException(String piece, Instant until) {
        super(piece + " asks to wait until " + until);
        this.piece = piece;
        this.until = until;
    }

    public String piece() {
        return piece;
    }

    public Instant until() {
        return until;
    }
}
