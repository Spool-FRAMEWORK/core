package software.spool.core.port.authentication;

import java.util.Objects;

/**
 * What a provider made of a request: who sent it, or why it could not tell. The reason is logged, so it never
 * holds the token, the signature or any other credential.
 */
public sealed interface Authentication {
    static Authentication accepted(Identity identity) {
        return new Accepted(identity);
    }

    static Authentication rejected(String reason) {
        return new Rejected(reason);
    }

    record Accepted(Identity identity) implements Authentication {
        public Accepted {
            Objects.requireNonNull(identity, "identity is required");
        }
    }

    record Rejected(String reason) implements Authentication {
        public Rejected {
            Objects.requireNonNull(reason, "reason is required");
        }
    }
}
