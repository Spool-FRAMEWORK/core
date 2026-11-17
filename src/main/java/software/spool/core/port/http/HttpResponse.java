package software.spool.core.port.http;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** What a route answers: a status, its headers and a body, which may be empty. */
public record HttpResponse(int status, Map<String, String> headers, byte[] body) {
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String JSON = "application/json";

    public HttpResponse {
        headers = Map.copyOf(headers);
        body = body == null ? new byte[0] : body.clone();
    }

    public static HttpResponse json(int status, String body) {
        return json(status, body.getBytes(StandardCharsets.UTF_8));
    }

    public static HttpResponse json(int status, byte[] body) {
        return new HttpResponse(status, Map.of(CONTENT_TYPE, JSON), body);
    }

    /** The same response with one more header. */
    public HttpResponse withHeader(String name, String value) {
        Map<String, String> more = new LinkedHashMap<>(headers);
        more.put(name, value);
        return new HttpResponse(status, more, body);
    }

    @Override
    public byte[] body() {
        return body.clone();
    }
}
