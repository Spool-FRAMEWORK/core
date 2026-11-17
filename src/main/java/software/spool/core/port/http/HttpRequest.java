package software.spool.core.port.http;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A request that reached the node, whole: what an authenticator needs to check it and what a route needs to answer it.
 * Header names are compared without case.
 */
public record HttpRequest(HttpMethod method, String path, Map<String, List<String>> headers, byte[] body) {
    public HttpRequest {
        TreeMap<String, List<String>> caseInsensitive = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        headers.forEach((name, values) -> caseInsensitive.put(name, List.copyOf(values)));
        headers = caseInsensitive;
        body = body == null ? new byte[0] : body.clone();
    }

    /** The first value of a header, or nothing when the request does not carry it. */
    public Optional<String> header(String name) {
        return Optional.ofNullable(headers.get(name)).filter(values -> !values.isEmpty()).map(List::getFirst);
    }

    @Override
    public byte[] body() {
        return body.clone();
    }
}
