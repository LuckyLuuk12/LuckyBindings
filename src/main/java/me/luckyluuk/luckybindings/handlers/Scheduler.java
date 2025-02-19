package me.luckyluuk.luckybindings.handlers;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Scheduler {
  private static final int availableProcessors = Runtime.getRuntime().availableProcessors();
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(availableProcessors / 2);

  /**
   * Run a task after a delay.
   * @param task The task to run
   * @param delay The delay in milliseconds before the task is run
   */
  public static ScheduledFuture<?> runLater(Runnable task, long delay) {
    return scheduler.schedule(task, delay*50L, TimeUnit.MILLISECONDS);
  }

  /**
   * Run a task repeatedly with a fixed delay and an optional initial delay.
   * @param task The task to run
   * @param period The delay between each run in milliseconds
   * @param initialDelay The optional initial delay in milliseconds before the first run, default is 20 milliseconds
   */
  public static ScheduledFuture<?> runRepeatedly(Runnable task, long period, long... initialDelay) {
    return scheduler.scheduleAtFixedRate(task, (initialDelay.length > 0 ? initialDelay[0] : 20)*50L, period * 50L, TimeUnit.MILLISECONDS);
  }
}