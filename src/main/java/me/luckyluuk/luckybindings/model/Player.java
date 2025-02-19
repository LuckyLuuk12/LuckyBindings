package me.luckyluuk.luckybindings.model;

import lombok.Getter;
import me.luckyluuk.luckybindings.handlers.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Getter
public class Player extends ClientPlayerEntity {
  protected MinecraftClient minecraftClient;

  public Player(MinecraftClient client, ClientWorld world, ClientPlayNetworkHandler networkHandler, StatHandler stats, ClientRecipeBook recipeBook, boolean lastSneaking, boolean lastSprinting) {
    super(client, world, networkHandler, stats, recipeBook, lastSneaking, lastSprinting);
    this.minecraftClient = client;
  }

  private boolean noPlayer() {
    return this.minecraftClient.player == null;
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
    if(noPlayer()) return;
    int step = steps.length > 0 ? steps[0] : 4;
    float centerYaw = minecraftClient.player.getHeadYaw();
    float yawIncrement = 16.0f / step;

    if (!agree) {
      for (int i = 0; i < step; i++) {
        final int index = i;
        Scheduler.runLater(() -> minecraftClient.player.setHeadYaw(centerYaw - yawIncrement * (index + 1)), i * 3000L / (step * 4L));
        Scheduler.runLater(() -> minecraftClient.player.setHeadYaw(centerYaw + yawIncrement * (index + 1)), (i + step) * 3000L / (step * 4L));
      }
      Scheduler.runLater(() -> minecraftClient.player.setHeadYaw(centerYaw), 3000L);
    } else {
      setPitch(0);
      for (int i = 0; i < step; i++) {
        Scheduler.runLater(() -> minecraftClient.player.setPitch(-15), i * 3000L / (step * 4L));
        Scheduler.runLater(() -> minecraftClient.player.setPitch(15), (i + step) * 3000L / (step * 4L));
      }
      Scheduler.runLater(() -> minecraftClient.player.setPitch(0), 3000L);
    }
  }

  public void sendMessage(String message) {
    if(noPlayer()) return;
    minecraftClient.player.sendMessage(Text.literal(message), false);
  }

  public void sendCommand(String command) {
    if(!command.startsWith("/")) command = "/" + command;
    this.networkHandler.sendCommand(command);
  }

  public void lookAtYaw(@Nullable BlockPos targetPos) {
    if(targetPos == null || noPlayer()) return;
    BlockPos playerPos = minecraftClient.player.getBlockPos();
    double deltaX = targetPos.getX() - playerPos.getX();
    double deltaZ = targetPos.getZ() - playerPos.getZ();

    // Calculate the angle in radians
    double angle = Math.atan2(deltaZ, deltaX);

    // Convert the angle to degrees
    double yaw = Math.toDegrees(angle) - 90; // Subtract 90 to adjust for Minecraft's yaw convention

    // Set the player's yaw
    minecraftClient.player.setHeadYaw((float) yaw);
  }

  public void lookAtPitch(@Nullable BlockPos targetPos) {
    if(targetPos == null || noPlayer()) return;
    BlockPos playerPos = minecraftClient.player.getBlockPos();
    double deltaY = targetPos.getY() - playerPos.getY();
    double deltaX = targetPos.getX() - playerPos.getX();
    double deltaZ = targetPos.getZ() - playerPos.getZ();

    // Calculate the distance in the horizontal plane
    double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    // Calculate the angle in radians for pitch
    double pitchAngle = Math.atan2(deltaY, horizontalDistance);
    // Convert the pitch angle to degrees
    double pitch = Math.toDegrees(pitchAngle);

    // Set the player's pitch
    minecraftClient.player.setHeadYaw((float) pitch);
  }

  public void lookAt(@Nullable BlockPos targetPos) {
    lookAtYaw(targetPos);
    lookAtPitch(targetPos);
  }

  public void moveTo(@Nullable BlockPos targetPos, boolean... sprint) {
    if(targetPos == null || noPlayer()) return;
    minecraftClient.player.setSprinting(sprint.length > 0 && sprint[0]);
    minecraftClient.player.move(MovementType.SELF, new Vec3d(targetPos.getX(), targetPos.getY(), targetPos.getZ()));
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
