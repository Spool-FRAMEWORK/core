package software.spool.core.utils.polling;

import software.spool.core.model.schedule.Schedule;

import java.time.Duration;

public record PollingConfiguration(PollingScheduler scheduler, Schedule schedule) {
    public static PollingConfiguration of(Schedule schedule) {
        return new PollingConfiguration(new ThreadedPollingScheduler(), schedule);
    }

    public static PollingConfiguration once() {
        return of(Schedule.once());
    }

    public static PollingConfiguration every(Duration interval) {
        return of(Schedule.every(interval));
    }

    public static PollingConfiguration defaults() {
        return PollingConfiguration.every(Duration.ofSeconds(15));
    }
}
