package software.spool.core.adapter.otel;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.UnaryOperator;

public record OTELExporterSettings(
        String endpoint,
        Map<String, String> headers,
        Map<String, String> headersFromEnvironment,
        String compression,
        Duration timeout
) {
    private static final String GENERAL_HEADERS_VARIABLE = "OTEL_EXPORTER_OTLP_HEADERS";

    public OTELExporterSettings {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        headersFromEnvironment = headersFromEnvironment == null ? Map.of() : Map.copyOf(headersFromEnvironment);
        if (compression != null && !compression.equals("gzip") && !compression.equals("none"))
            throw new IllegalArgumentException("Unsupported OTLP compression '" + compression + "', use gzip or none");
    }

    public static OTELExporterSettings none() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public OTELExporterSettings withEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) return this;
        return new OTELExporterSettings(endpoint, headers, headersFromEnvironment, compression, timeout);
    }

    public OTELExporterSettings overriding(OTELExporterSettings base) {
        Map<String, String> mergedHeaders = new LinkedHashMap<>(base.headers);
        mergedHeaders.putAll(headers);
        Map<String, String> mergedHeadersFromEnvironment = new LinkedHashMap<>(base.headersFromEnvironment);
        mergedHeadersFromEnvironment.putAll(headersFromEnvironment);
        return new OTELExporterSettings(
                endpoint != null ? endpoint : base.endpoint,
                mergedHeaders,
                mergedHeadersFromEnvironment,
                compression != null ? compression : base.compression,
                timeout != null ? timeout : base.timeout);
    }

    public Map<String, String> resolveHeaders(String signal, UnaryOperator<String> environment) {
        Map<String, String> resolved = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        resolved.putAll(parse(environment.apply(GENERAL_HEADERS_VARIABLE), GENERAL_HEADERS_VARIABLE));
        String signalVariable = "OTEL_EXPORTER_OTLP_" + signal + "_HEADERS";
        resolved.putAll(parse(environment.apply(signalVariable), signalVariable));
        resolved.putAll(headers);
        headersFromEnvironment.forEach((header, variable) -> resolved.put(header, valueOf(header, variable, environment)));
        return resolved;
    }

    private static Map<String, String> parse(String raw, String variable) {
        Map<String, String> parsed = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) return parsed;
        for (String pair : raw.split(",")) {
            int separator = pair.indexOf('=');
            if (separator <= 0)
                throw new IllegalArgumentException(
                        "The environment variable '" + variable + "' must hold name=value pairs separated by commas");
            parsed.put(pair.substring(0, separator).trim(), decode(pair.substring(separator + 1).trim()));
        }
        return parsed;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }

    private static String valueOf(String header, String variable, UnaryOperator<String> environment) {
        String value = environment.apply(variable);
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(
                    "The environment variable '" + variable + "' named for the header '" + header + "' is not set");
        return value;
    }

    @Override
    public String toString() {
        return "OTELExporterSettings[endpoint=" + endpoint
                + ", headers=" + headers.keySet()
                + ", headersFromEnvironment=" + headersFromEnvironment
                + ", compression=" + compression
                + ", timeout=" + timeout + "]";
    }

    public static class Builder {
        private String endpoint;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private final Map<String, String> headersFromEnvironment = new LinkedHashMap<>();
        private String compression;
        private Duration timeout;

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Builder headerFromEnvironment(String name, String variable) {
            headersFromEnvironment.put(name, variable);
            return this;
        }

        public Builder compression(String compression) {
            this.compression = compression;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public OTELExporterSettings build() {
            return new OTELExporterSettings(endpoint, headers, headersFromEnvironment, compression, timeout);
        }
    }
}
