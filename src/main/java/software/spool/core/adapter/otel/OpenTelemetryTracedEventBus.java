package software.spool.core.adapter.otel;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.*;
import software.spool.core.model.Event;
import software.spool.core.port.tracing.TracedEventBus;
import software.spool.core.port.tracing.TraceScope;

import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OpenTelemetryTracedEventBus implements TracedEventBus {
    private final Tracer tracer;

    public OpenTelemetryTracedEventBus() {
        OTELConfig.init(Objects.requireNonNullElse(System.getenv("SERVICE_NAME"), "unknown"));
        tracer = GlobalOpenTelemetry.getTracer("spool.eventbus");
    }

    @Override
    public <E extends Event> TraceScope send(E event) {
        Span span = getSpanFrom(event);
        return new TraceScope() {
            @Override
            public void error(Exception error) {
                span.setStatus(StatusCode.ERROR);
                span.recordException(error);
            }
            @Override
            public void end() {span.end();}
        };
    }

    private <E extends Event> Span getSpanFrom(E event) {
        SpanBuilder builder = tracer.spanBuilder(event.eventType());
        recordToMap(event).forEach((key, value) ->
                builder.setAttribute(key, Objects.toString(value)));
        return builder.startSpan();
    }

    private Map<String, Object> recordToMap(Object record) {
        Map<String, Object> map = new HashMap<>();
        for (RecordComponent component : record.getClass().getRecordComponents()) {
            try {
                var accessor = component.getAccessor();
                map.put(component.getName(), accessor.invoke(record));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }
}
