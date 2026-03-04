package software.spool.core.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Immutable value object representing the identifier of a payload
 * to avoid duplication.
 *
 * <p>
 * Use {@link #of(String)} as a named constructor to obtain instances.
 * </p>
 *
 * @param value the raw string identifier; must not be {@code null}
 */
public record IdempotencyKey(String value) {
    public IdempotencyKey(final String value) {
        this.value = Objects.requireNonNull(value);
    }

    /**
     * Creates a new {@code InboxEntryId} wrapping the given string value.
     *
     * @param idempotencyKey the raw identifier string; must not be {@code null}
     * @return a new {@code InboxEntryId} instance
     */
    public static IdempotencyKey of(String idempotencyKey) {
        return new IdempotencyKey(idempotencyKey);
    }

    /**
     * Derives a deterministic idempotency key from the given payload.
     *
     * <p>
     * The key is a lowercase hex SHA-256 digest of
     * {@code sourceId + ":" + payload}.
     * </p>
     *
     * @param payload the serialized record payload
     * @return an idempotency key of 64-character hex string
     * @throws IllegalStateException if the SHA-256 algorithm is not available
     */
    private IdempotencyKey generateIdempotencyKeyFrom(String sourceId, String payload) throws IllegalStateException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = sourceId + ":" + payload;
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return new IdempotencyKey(HexFormat.of().formatHex(hash));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
