package software.spool.core.port.health;

import java.time.Instant;
import java.util.Optional;

/**
 * The health of a piece of infrastructure a module uses.
 *
 * @param piece     the name of the piece in the descriptor
 * @param type      the type of the piece, the plugin that builds it, such as {@code S3}
 * @param status    how the piece is doing
 * @param reason    why it is not healthy, when it is not
 * @param checkedAt when its health was worked out
 * @param hasCheck  whether the piece has a check of its own; without one it is judged only by the calls to it
 */
public record PieceHealth(String piece, String type, HealthStatus status, Optional<String> reason, Instant checkedAt,
                          boolean hasCheck) {
}
