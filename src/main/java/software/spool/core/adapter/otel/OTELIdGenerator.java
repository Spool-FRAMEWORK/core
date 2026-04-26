package software.spool.core.adapter.otel;

import io.opentelemetry.sdk.trace.IdGenerator;

public final class OTELIdGenerator implements IdGenerator {
    public static final OTELIdGenerator INSTANCE = new OTELIdGenerator();
    private static final ThreadLocal<String> PENDING_TRACE_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> PENDING_SPAN_ID  = new ThreadLocal<>();

    private OTELIdGenerator() {}

    static void inject(String traceId, String spanId) {
        if (traceId != null) PENDING_TRACE_ID.set(traceId);
        if (spanId != null) PENDING_SPAN_ID.set(spanId);
    }

    static void clear() {
        PENDING_TRACE_ID.remove();
        PENDING_SPAN_ID.remove();
    }

    @Override
    public String generateSpanId() {
        String id = PENDING_SPAN_ID.get();
        if (id != null) { PENDING_SPAN_ID.remove(); return id; }
        return IdGenerator.random().generateSpanId();
    }

    @Override
    public String generateTraceId() {
        String id = PENDING_TRACE_ID.get();
        if (id != null) { PENDING_TRACE_ID.remove(); return id; }
        return IdGenerator.random().generateTraceId();
    }
}