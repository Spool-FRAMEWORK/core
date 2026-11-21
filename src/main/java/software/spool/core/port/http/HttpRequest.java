package software.spool.core.port.http;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A request that reached the node, whole: what an authenticator needs to check it and what a route needs to answer it.
 * Header names are compared without case. The variable parts of its route ({@code /records/{key}}) and the
 * parameters of its URL come decoded.
 */
public record HttpRequest(HttpMethod method, String path, Map<String, List<String>> headers, byte[] body,
                          Map<String, String> pathParameters, Map<String, List<String>> queryParameters) {
    public HttpRequest {
        TreeMap<String, List<String>> caseInsensitive = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.forEach((name, values) -> caseInsensitive.put(name, List.copyOf(values)));
        headers = caseInsensitive;
        body = body == null ? new byte[0] : body.clone();
        pathParameters = Map.copyOf(pathParameters);
        queryParameters = Map.copyOf(queryParameters);
    }

    public HttpRequest(HttpMethod method, String path, Map<String, List<String>> headers, byte[] body) {
        this(method, path, headers, body, Map.of(), Map.of());
    }

    /** The first value of a header, or nothing when the request does not carry it. */
    public Optional<String> header(String name) {
        return Optional.ofNullable(headers.get(name)).filter(values -> !values.isEmpty()).map(List::getFirst);
    }

    /** The value of a variable part of the route, such as {@code key} in {@code /records/{key}}. */
    public Optional<String> pathParameter(String name) {
        return Optional.ofNullable(pathParameters.get(name));
    }

    /** The first value of a parameter of the URL, or nothing when the URL does not carry it. */
    public Optional<String> queryParameter(String name) {
        return Optional.ofNullable(queryParameters.get(name)).filter(values -> !values.isEmpty()).map(List::getFirst);
    }

    /** The same request with the variable parts of the route it matched. */
    public HttpRequest withPathParameters(Map<String, String> parameters) {
        return new HttpRequest(method, path, headers, body, parameters, queryParameters);
    }

    @Override
    public byte[] body() {
        return body.clone();
    }
}
