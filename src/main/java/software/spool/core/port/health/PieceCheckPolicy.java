package software.spool.core.port.health;

import java.time.Duration;

/**
 * How often a piece is checked and when a check counts as slow.
 *
 * @param interval  the shortest time between two checks of the piece; in between, the last result is reused
 * @param slowAfter a check that takes longer than this leaves the piece degraded
 */
public record PieceCheckPolicy(Duration interval, Duration slowAfter) {
    public static final Duration DEFAULT_INTERVAL = Duration.ofSeconds(10);
    public static final Duration DEFAULT_SLOW_AFTER = Duration.ofSeconds(2);

    public static PieceCheckPolicy defaults() {
        return new PieceCheckPolicy(DEFAULT_INTERVAL, DEFAULT_SLOW_AFTER);
    }
}
