package software.spool.core.port.http;

import software.spool.core.port.authentication.Identity;

/** Who called a route: nobody in particular on an open route, or the identity the provider gave. */
public sealed interface Caller {
    static Caller anonymous() {
        return new Anonymous();
    }

    record Anonymous() implements Caller {
    }

    record Identified(Identity identity) implements Caller {
    }
}
