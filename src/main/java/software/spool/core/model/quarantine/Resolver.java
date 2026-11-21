package software.spool.core.model.quarantine;

import java.util.Objects;

/**
 * Who resolved a stay in quarantine: the identity the authentication provider gave, or nobody known for the stays
 * resolved before resolvers were kept.
 */
public sealed interface Resolver {

    static Resolver identified(String subject, String provider) {
        return new Identified(subject, provider);
    }

    static Resolver unknown() {
        return new Unknown();
    }

    record Identified(String subject, String provider) implements Resolver {
        public Identified {
            Objects.requireNonNull(subject, "subject is required");
            Objects.requireNonNull(provider, "provider is required");
        }
    }

    record Unknown() implements Resolver {
    }
}
