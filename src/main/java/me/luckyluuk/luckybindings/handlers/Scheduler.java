package me.luckyluuk.luckybindings.handlers;

import me.luckyluuk.luckybindings.LuckyBindings;
import net.minecraft.client.MinecraftClient;

public class Scheduler {
  public static void runLater(Runnable task, long delay) {
    MinecraftClient client = MinecraftClient.getInstance();
    client.execute(() -> {
      try {
        Thread.sleep(delay);
        task.run();
      } catch (InterruptedException e) {
        LuckyBindings.LOGGER.error("Error while running task later: {}", e.getMessage());
      }
    });
  }
}
