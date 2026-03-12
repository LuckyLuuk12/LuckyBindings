package nl.kablan.luckybindings.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A scheduler for running tasks asynchronously with delay and repetition support.
 */
public class Scheduler {
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);

    /**
     * Run a task after a delay (in game ticks).
     * @param task The task to run
     * @param delayTicks The delay in game ticks before the task is run
     */
    public static ScheduledFuture<?> runLater(SelfManagingTask task, long delayTicks) {
        ScheduledFuture<?>[] futureHolder = new ScheduledFuture<?>[1];
        futureHolder[0] = SCHEDULER.schedule(() -> task.run(futureHolder[0]), delayTicks * 50L, TimeUnit.MILLISECONDS);
        return futureHolder[0];
    }

    /**
     * Run a task repeatedly with a fixed delay and an optional initial delay.
     * @param task The task to run, which accepts its own ScheduledFuture as an argument
     * @param periodTicks The delay between each run in game ticks
     * @param initialDelayTicks The optional initial delay in game ticks before the first run
     */
    public static ScheduledFuture<?> runRepeatedly(SelfManagingTask task, long periodTicks, long... initialDelayTicks) {
        ScheduledFuture<?>[] futureHolder = new ScheduledFuture<?>[1];
        long initialMs = (initialDelayTicks.length > 0 ? initialDelayTicks[0] : 1) * 50L;
        long periodMs = periodTicks * 50L;
        futureHolder[0] = SCHEDULER.scheduleAtFixedRate(() -> task.run(futureHolder[0]), initialMs, periodMs, TimeUnit.MILLISECONDS);
        return futureHolder[0];
    }

    @FunctionalInterface
    public interface SelfManagingTask {
        void run(ScheduledFuture<?> self);
    }
}