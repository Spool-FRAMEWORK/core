package software.spool.core.adapter.http;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The shape of a route path, made of literal parts and variable parts written {@code {name}}. A path matches when it
 * has as many parts and every literal part is the same; the variable parts give their values.
 */
final class RoutePattern {
    private static final String SEPARATOR = "/";
    private static final String VARIABLE_START = "{";
    private static final String VARIABLE_END = "}";

    private final String path;
    private final List<String> parts;

    RoutePattern(String path) {
        this.path = path;
        this.parts = partsOf(path);
    }

    String path() {
        return path;
    }

    long literalParts() {
        return parts.stream().filter(part -> !isVariable(part)).count();
    }

    Optional<Map<String, String>> match(List<String> requestParts) {
        if (requestParts.size() != parts.size()) return Optional.empty();
        Map<String, String> values = new LinkedHashMap<>();
        for (int position = 0; position < parts.size(); position++) {
            String part = parts.get(position);
            String requested = requestParts.get(position);
            if (isVariable(part)) {
                values.put(part.substring(VARIABLE_START.length(), part.length() - VARIABLE_END.length()), requested);
            } else if (!part.equals(requested)) {
                return Optional.empty();
            }
        }
        return Optional.of(values);
    }

    static List<String> partsOf(String path) {
        return Arrays.stream(path.split(SEPARATOR)).filter(part -> !part.isEmpty()).toList();
    }

    private static boolean isVariable(String part) {
        return part.startsWith(VARIABLE_START) && part.endsWith(VARIABLE_END);
    }
}
