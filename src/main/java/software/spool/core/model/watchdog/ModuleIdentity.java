package software.spool.core.model.watchdog;

import java.util.UUID;

public record ModuleIdentity(String moduleId) {

    public static ModuleIdentity of(String moduleId) {
        return new ModuleIdentity(moduleId);
    }

    public static ModuleIdentity random(String prefix) {
        return new ModuleIdentity(prefix + "-" + UUID.randomUUID().toString().substring(0, 8));
    }
}