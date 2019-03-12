package scheduler;

import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;

public class ScheduledJob {
    private static final long ONE_HOUR = 60 * 60 * 1000L;
    private static final long THREE_MINUTES = 3 * 60 * 1000L;

    @Scheduled(cron = "${batch.cron}")
    @SchedulerLock(name = "", lockAtLeastFor = THREE_MINUTES, lockAtMostFor = ONE_HOUR)
    public void deleteExpiredNotificationDataEntries(){
        // what you want to do in the job
    }
}
