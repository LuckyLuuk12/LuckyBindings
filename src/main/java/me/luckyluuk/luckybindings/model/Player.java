package me.luckyluuk.luckybindings.model;

import me.luckyluuk.luckybindings.actions.Action;
import me.luckyluuk.luckybindings.handlers.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class Player extends ClientPlayerEntity {
  public Player(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting) {
    super(client, world, networkHandler, stats, recipeBook, lastSneaking, lastSprinting);
  }

  public void executeAction(Action action) {
    action.execute(this);
  }

  /**
   * Let the player nod his head. The head will be moving:
   * from Center to right, left, right, left, right to center. Or center to down, up, down, up, down, center.
   * In a time span of 3 seconds with 3000 / steps milliseconds between each step.
   * The pitch will be reset to 0 if it is not already 0 if the player agrees.
   * @param agree Whether the player should nod in agreement or disagreement.
   * @param steps The optional number of steps from left to right or up to down. Default is 4. Works best if it divides 16.
   */
  public void nodHead(boolean agree, int... steps) {
    int step = steps.length > 0 ? steps[0] : 4;
    float centerYaw = this.getHeadYaw();
    float yawIncrement = 16.0f / step;

    if (!agree) {
      for (int i = 0; i < step; i++) {
        final int index = i;
        Scheduler.runLater(() -> this.setHeadYaw(centerYaw - yawIncrement * (index + 1)), i * 3000L / (step * 4L));
        Scheduler.runLater(() -> this.setHeadYaw(centerYaw + yawIncrement * (index + 1)), (i + step) * 3000L / (step * 4L));
      }
      Scheduler.runLater(() -> this.setHeadYaw(centerYaw), 3000L);
    } else {
      this.setPitch(0);
      for (int i = 0; i < step; i++) {
        Scheduler.runLater(() -> this.setPitch(-15), i * 3000L / (step * 4L));
        Scheduler.runLater(() -> this.setPitch(15), (i + step) * 3000L / (step * 4L));
      }
      Scheduler.runLater(() -> this.setPitch(0), 3000L);
    }
  }

  public void sendMessage(String message) {
    this.sendMessage(Text.literal(message), false);
  }

  public void sendCommand(String command) {
    if(!command.startsWith("/")) command = "/" + command;
    this.networkHandler.sendCommand(command);
  }
  /**
   * Converts a ClientPlayerEntity to a Player.
   * @param p The player to convert.
   * @return The converted player.
   */
  static public Player from(@Nullable ClientPlayerEntity p) {
    if (p == null) return null;
    return new Player(MinecraftClient.getInstance(), (ClientWorld) p.getWorld(), p.networkHandler, p.getStatHandler(), p.getRecipeBook(), p.isSneaking(), p.isSprinting());
  }
}
