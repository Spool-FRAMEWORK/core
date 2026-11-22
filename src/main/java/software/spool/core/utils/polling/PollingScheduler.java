package software.spool.core.utils.polling;

import software.spool.core.model.schedule.Schedule;

public interface PollingScheduler {
    void schedule(Runnable task, Schedule schedule, CancellationToken token);
}
