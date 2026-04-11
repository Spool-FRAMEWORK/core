package software.spool.core.adapter.otel;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.*;
import io.opentelemetry.context.Context;
import org.slf4j.MDC;
import software.spool.core.model.Event;
import software.spool.core.model.SpoolEvent;
import software.spool.core.port.tracing.TracedEventBus;
import software.spool.core.port.tracing.TraceScope;

import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class OpenTelemetryTracedEventBus implements TracedEventBus {
    private final Tracer tracer;

    public OpenTelemetryTracedEventBus() {
        this.tracer = GlobalOpenTelemetry.getTracer("spool.eventbus");
    }

    @Override
    public <E extends Event> TraceScope send(E event) {
        Span span = buildSpan(event);
        io.opentelemetry.context.Scope otelScope = span.makeCurrent();
        MDC.put("traceId", span.getSpanContext().getTraceId());
        MDC.put("spanId",  span.getSpanContext().getSpanId());
        AtomicBoolean closed = new AtomicBoolean(false);

        return new TraceScope() {
            @Override
            public void error(Exception e) {
                span.setStatus(StatusCode.ERROR);
                span.recordException(e);
            }

            @Override
            public void end() {
                if (closed.compareAndSet(false, true)) {
                    MDC.remove("traceId");
                    MDC.remove("spanId");
                    otelScope.close();
                    span.end();
                }
            }
        };
    }

    private <E extends Event> Span buildSpan(E event) {
        SpanBuilder builder = tracer.spanBuilder(event.eventType());

        if (event instanceof SpoolEvent spoolEvent) {
            applyCorrelation(builder, spoolEvent);
        }

        recordToMap(event).forEach((key, value) ->
                builder.setAttribute(key, Objects.toString(value)));

        try {
            return builder.startSpan();
        } finally {
            OTELIdGenerator.clear();
        }
    }

    private void applyCorrelation(SpanBuilder builder, SpoolEvent event) {
        String correlationId = event.correlationId();
        if (correlationId == null || correlationId.isBlank()) return;
        String traceId = toTraceId(correlationId);
        String mySpanId = toSpanId(event.eventId());
        String causationId = event.causationId();
        if (causationId != null && !causationId.isBlank()) {
            SpanContext remoteParent = SpanContext.createFromRemoteParent(
                    traceId,
                    toSpanId(causationId),
                    TraceFlags.getSampled(),
                    TraceState.getDefault()
            );
            builder.setParent(Context.root().with(Span.wrap(remoteParent)));
            OTELIdGenerator.inject(null, mySpanId);
        } else {
            OTELIdGenerator.inject(traceId, mySpanId);
        }
    }

    private String toTraceId(String uuid) {
        return uuid.replace("-", "");
    }

    private String toSpanId(String uuid) {
        return uuid.replace("-", "").substring(0, 16);
    }

    private Map<String, Object> recordToMap(Object record) {
        Map<String, Object> map = new HashMap<>();
        for (RecordComponent c : record.getClass().getRecordComponents()) {
            try {
                map.put(c.getName(), c.getAccessor().invoke(record));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }
}