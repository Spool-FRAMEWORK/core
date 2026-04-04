package software.spool.core.adapter.otel;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import software.spool.core.model.watchdog.ModuleIdentity;
import software.spool.core.port.watchdog.ModuleLogger;

public class OpenTelemetryModuleLogger implements ModuleLogger {
    private static final AttributeKey<String> MODULE_ID = AttributeKey.stringKey("module.id");
    private static final AttributeKey<String> REASON = AttributeKey.stringKey("degraded.reason");
    private final Logger logger;

    public OpenTelemetryModuleLogger() {
        OTELConfig.init("watchdog");
        this.logger = GlobalOpenTelemetry.get()
                .getLogsBridge()
                .get("spool.watchdog");
    }

    @Override
    public void moduleStarted(ModuleIdentity identity) {
        emit(Severity.INFO, "Module started", identity, null);
    }

    @Override
    public void moduleStopped(ModuleIdentity identity) {
        emit(Severity.WARN, "Module stopped", identity, null);
    }

    @Override
    public void moduleDegraded(ModuleIdentity identity, String reason) {
        emit(Severity.ERROR, "Module degraded", identity, reason);
    }

    private void emit(Severity severity, String message, ModuleIdentity identity, String reason) {
        LogRecordBuilder builder = logger.logRecordBuilder()
                .setSeverity(severity)
                .setBody(message)
                .setAttribute(MODULE_ID, identity.moduleId());
        if (reason != null)
            builder.setAttribute(REASON, reason);
        builder.emit();
    }
}