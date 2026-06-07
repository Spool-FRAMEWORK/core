package software.spool.core.port.metrics;

public final class SpoolMetrics {
    private SpoolMetrics() {}

    public static String named(String base, String moduleId) {
        return base + "." + moduleId;
    }

    public static final class Crawler {
        private Crawler() {}

        public static final String EVENTS_TOTAL = "spool.crawler.events.total";
        public static final String EVENTS_TOTAL_DESC = "Events captured by the crawler";
        public static final String ERRORS_TOTAL = "spool.crawler.errors.total";
        public static final String ERRORS_TOTAL_DESC = "Failed crawls";
        public static final String LATENCY = "spool.crawler.latency";
        public static final String LATENCY_DESC = "Time from detection to ingester delivery";
    }

    public static final class Ingester {
        private Ingester() {}

        public static final String RECORDS_TOTAL = "spool.ingester.records.total";
        public static final String RECORDS_TOTAL_DESC = "Successfully ingested records";
        public static final String RECORDS_REJECTED_TOTAL = "spool.ingester.records.rejected.total";
        public static final String RECORDS_REJECTED_TOTAL_DESC = "Records discarded by validation";
        public static final String PROCESSING_DURATION = "spool.ingester.processing.duration";
        public static final String PROCESSING_DURATION_DESC = "Processing and validation duration per record";
        public static final String RECORD_SIZE = "spool.ingester.record.size";
        public static final String RECORD_SIZE_DESC = "Size of ingested records";
    }

    public static final class Mounter {
        private Mounter() {}

        public static final String DATASETS_MOUNTED_TOTAL = "spool.mounter.datasets.mounted.total";
        public static final String DATASETS_MOUNTED_TOTAL_DESC = "Successfully mounted datasets";
        public static final String REMOUNTS_TOTAL = "spool.mounter.remounts.total";
        public static final String REMOUNTS_TOTAL_DESC = "Dataset remount operations";
        public static final String MOUNT_LATENCY = "spool.mounter.mount.latency";
        public static final String MOUNT_LATENCY_DESC = "Time from ingestion end to dataset availability";
    }

    public static final class Janitor {
        private Janitor() {}

        public static final String RECORDS_CLEANED_TOTAL = "spool.janitor.records.cleaned.total";
        public static final String RECORDS_CLEANED_TOTAL_DESC = "Deleted or expired records";
        public static final String CYCLES_COMPLETED_TOTAL = "spool.janitor.cycles.completed.total";
        public static final String CYCLES_COMPLETED_TOTAL_DESC = "Successfully completed cleanup cycles";
        public static final String CYCLES_FAILED_TOTAL = "spool.janitor.cycles.failed.total";
        public static final String CYCLES_FAILED_TOTAL_DESC = "Failed cleanup cycles";
        public static final String CYCLE_DURATION = "spool.janitor.cycle.duration";
        public static final String CYCLE_DURATION_DESC = "Duration of a full cleanup cycle";
    }

    public static final class Module {
        private Module() {}

        public static final String STARTED_TOTAL = "spool.module.started.total";
        public static final String STARTED_TOTAL_DESC = "Started modules";
        public static final String STOPPED_TOTAL = "spool.module.stopped.total";
        public static final String STOPPED_TOTAL_DESC = "Stopped modules";
        public static final String DEGRADED_TOTAL = "spool.module.degraded.total";
        public static final String DEGRADED_TOTAL_DESC = "Degraded modules";
        public static final String ACTIVE = "spool.module.active";
        public static final String ACTIVE_DESC = "Currently running modules";
    }

    public static final class Watchdog {
        private Watchdog() {}

        public static final String HEARTBEATS_TOTAL      = "spool.watchdog.heartbeats.total";
        public static final String HEARTBEATS_TOTAL_DESC = "Heartbeats received from modules";
        public static final String TIMEOUTS_TOTAL        = "spool.watchdog.timeouts.total";
        public static final String TIMEOUTS_TOTAL_DESC   = "Modules that missed their heartbeat deadline";
        public static final String ZOMBIES_TOTAL         = "spool.watchdog.zombies.total";
        public static final String ZOMBIES_TOTAL_DESC    = "Zombie modules removed from registry";
        public static final String CHECK_DURATION        = "spool.watchdog.check.duration";
        public static final String CHECK_DURATION_DESC   = "Duration of each monitoring check cycle";
        public static final String DOWNTIME_DURATION          = "spool.watchdog.module.downtime";
        public static final String DOWNTIME_DURATION_DESC     = "Time a module spent in DEGRADED state before recovering";
        public static final String HEALTHY_RATIO              = "spool.watchdog.healthy.ratio";
        public static final String HEALTHY_RATIO_DESC         = "Percentage of healthy modules out of all tracked modules (0-100)";
        public static final String MODULES_ACTIVE             = "spool.watchdog.modules.active";
        public static final String MODULES_ACTIVE_DESC        = "Currently tracked modules in the watchdog registry";
        public static final String MODULE_STARTED_TOTAL       = "spool.watchdog.module.started.total";
        public static final String MODULE_STARTED_TOTAL_DESC  = "Modules seen for the first time by the watchdog";
        public static final String MODULE_STOPPED_TOTAL       = "spool.watchdog.module.stopped.total";
        public static final String MODULE_STOPPED_TOTAL_DESC  = "Modules evicted from the watchdog registry as zombies";
        public static final String MODULE_DEGRADED_TOTAL      = "spool.watchdog.module.degraded.total";
        public static final String MODULE_DEGRADED_TOTAL_DESC = "Modules marked DEGRADED by the watchdog";
    }

    public static final class Attributes {
        private Attributes() {}

        public static final String SOURCE = "source";
        public static final String INGESTER = "ingester";
        public static final String TYPE = "type";
        public static final String FORMAT = "format";
        public static final String POLICY = "policy";
        public static final String STATUS = "status";
        public static final String REASON = "reason";
        public static final String MODULE = "module";
        public static final String DATASET = "dataset";
    }
}
