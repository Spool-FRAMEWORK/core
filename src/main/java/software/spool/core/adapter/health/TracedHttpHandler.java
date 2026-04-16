package software.spool.core.adapter.health;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;

import java.io.IOException;

public class TracedHttpHandler implements HttpHandler {
    private final HttpHandler delegate;
    private final Tracer tracer;

    private static final TextMapGetter<HttpExchange> GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(HttpExchange carrier) {
            return carrier.getRequestHeaders().keySet();
        }

        @Override
        public String get(HttpExchange carrier, String key) {
            if (carrier != null && carrier.getRequestHeaders().containsKey(key)) {
                return carrier.getRequestHeaders().getFirst(key);
            }
            return null;
        }
    };

    public TracedHttpHandler(HttpHandler delegate) {
        this.delegate = delegate;
        this.tracer = GlobalOpenTelemetry.getTracer("spool.http.server");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Context extractedContext = GlobalOpenTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), exchange, GETTER);
        Span span = tracer.spanBuilder(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath())
                .setParent(extractedContext)
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
        try (Scope scope = span.makeCurrent()) {
            span.setAttribute("http.method", exchange.getRequestMethod());
            span.setAttribute("http.route", exchange.getRequestURI().getPath());
            delegate.handle(exchange);
            int statusCode = exchange.getResponseCode();
            span.setAttribute("http.status_code", statusCode != -1 ? statusCode : 200);
            if (statusCode >= 500) span.setStatus(StatusCode.ERROR);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR);
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
}