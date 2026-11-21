package software.spool.core.adapter.jackson;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.module.SimpleModule;
import software.spool.core.model.quarantine.QuarantineLocation;
import software.spool.core.model.quarantine.QuarantineResolution;
import software.spool.core.model.quarantine.Resolver;

/**
 * Tells Jackson which variant of each quarantine type a JSON object is, through a {@code type} field, so the model
 * does not depend on Jackson.
 */
final class QuarantineModule extends SimpleModule {

    QuarantineModule() {
        super("spool-quarantine");
        setMixInAnnotation(QuarantineLocation.class, LocationMixin.class);
        setMixInAnnotation(QuarantineResolution.class, ResolutionMixin.class);
        setMixInAnnotation(Resolver.class, ResolverMixin.class);
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = QuarantineLocation.Inbox.class, name = "INBOX"),
            @JsonSubTypes.Type(value = QuarantineLocation.Bronze.class, name = "BRONZE")
    })
    private interface LocationMixin {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = QuarantineResolution.Pending.class, name = "PENDING"),
            @JsonSubTypes.Type(value = QuarantineResolution.Republished.class, name = "REPUBLISHED"),
            @JsonSubTypes.Type(value = QuarantineResolution.Corrected.class, name = "CORRECTED"),
            @JsonSubTypes.Type(value = QuarantineResolution.Bypassed.class, name = "BYPASSED"),
            @JsonSubTypes.Type(value = QuarantineResolution.Purged.class, name = "PURGED")
    })
    private interface ResolutionMixin {
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Resolver.Identified.class, name = "IDENTIFIED"),
            @JsonSubTypes.Type(value = Resolver.Unknown.class, name = "UNKNOWN")
    })
    private interface ResolverMixin {
    }
}
