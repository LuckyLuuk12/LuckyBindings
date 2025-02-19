package me.luckyluuk.luckybindings.handlers;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {
  private static final int availableProcessors = Runtime.getRuntime().availableProcessors();
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(availableProcessors);

  /**
   * Run a task after a delay.
   * @param task The task to run
   * @param delay The delay in milliseconds before the task is run
   */
  public static void runLater(Runnable task, long delay) {
    scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
  }

  /**
   * Run a task repeatedly with a fixed delay and an optional initial delay.
   * @param task The task to run
   * @param period The delay between each run in milliseconds
   * @param initialDelay The optional initial delay in milliseconds before the first run, default is 20 milliseconds
   */
  public static void runRepeatedly(Runnable task, long period, long... initialDelay) {
    scheduler.scheduleAtFixedRate(task, initialDelay.length > 0 ? initialDelay[0] : 20, period, TimeUnit.MILLISECONDS);
  }
}