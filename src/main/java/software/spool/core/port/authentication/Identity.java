package software.spool.core.port.authentication;

import java.util.Objects;
import java.util.Set;

/** Who sent a request, as the provider that identified it says, with the roles it holds. */
public record Identity(String subject, Set<String> roles, String provider) {
    public Identity {
        Objects.requireNonNull(subject, "subject is required");
        Objects.requireNonNull(provider, "provider is required");
        roles = Set.copyOf(roles);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
