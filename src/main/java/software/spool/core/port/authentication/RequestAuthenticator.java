package software.spool.core.port.authentication;

import software.spool.core.port.http.HttpRequest;

/**
 * Tells who sent a request, from the whole request: method, path, headers and body (ADR-013). It does not decide
 * what the caller may do; each route declares the role it needs and the node checks it.
 */
@FunctionalInterface
public interface RequestAuthenticator {
    Authentication authenticate(HttpRequest request);
}
