package me.luckyluuk.luckybindings.handlers;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {
  private static final int availableProcessors = Runtime.getRuntime().availableProcessors();
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(availableProcessors);

  public static void runLater(Runnable task, long delay) {
    scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
  }
}