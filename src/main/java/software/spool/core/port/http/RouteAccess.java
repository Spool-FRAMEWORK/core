package software.spool.core.port.http;

import software.spool.core.port.authentication.RequestAuthenticator;

import java.util.Objects;

/**
 * Who may call a route. Only the health of the node is {@link Open}: every other route is {@link Protected} by the
 * provider that identifies the caller and the role the caller needs (ADR-013).
 */
public sealed interface RouteAccess {
    static RouteAccess open() {
        return new Open();
    }

    record Open() implements RouteAccess {
    }

    record Protected(RequestAuthenticator authenticator, String role) implements RouteAccess {
        public Protected {
            Objects.requireNonNull(authenticator, "authenticator is required");
            if (role == null || role.isBlank()) throw new IllegalArgumentException("A protected route needs a role");
        }
    }
}
