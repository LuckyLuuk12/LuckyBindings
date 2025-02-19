package me.luckyluuk.luckybindings.model;

import lombok.Getter;
import me.luckyluuk.luckybindings.handlers.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Getter
public class PlayerUtil {
  static public ClientPlayerEntity getPlayer() {
    return MinecraftClient.getInstance().player;
  }
  static private boolean noPlayer() {
    return getPlayer() == null;
  }

  /**
   * Let the player nod his head. The head will be moving:
   * from Center to right, left, right, left, right to center. Or center to down, up, down, up, down, center.
   * In a time span of 3 seconds with 3000 / `steps` milliseconds between each step.
   * The pitch will be reset to 0 if it is not already 0 if the player agrees.
   * @param agree Whether the player should nod in agreement or disagreement.
   * @param steps The optional number of steps from left to right or up to down. Default is 4. Works best if it divides 16.
   */
  static public void nodHead(boolean agree, int... steps) {
    if(noPlayer()) return;
    int step = steps.length > 0 ? steps[0] : 4;
    float centerYaw = getPlayer().getHeadYaw();
    float yawIncrement = 16.0f / step;

    if (!agree) {
      for (int i = 0; i < step; i++) {
        final int index = i;
        Scheduler.runLater(() -> getPlayer().setHeadYaw(centerYaw - yawIncrement * (index + 1)), i * 3000L / (step * 4L));
        Scheduler.runLater(() -> getPlayer().setHeadYaw(centerYaw + yawIncrement * (index + 1)), (i + step) * 3000L / (step * 4L));
      }
      Scheduler.runLater(() -> getPlayer().setHeadYaw(centerYaw), 3000L);
    } else {
      getPlayer().setPitch(0);
      for (int i = 0; i < step; i++) {
        Scheduler.runLater(() -> getPlayer().setPitch(-15), i * 3000L / (step * 4L));
        Scheduler.runLater(() -> getPlayer().setPitch(15), (i + step) * 3000L / (step * 4L));
      }
      Scheduler.runLater(() -> getPlayer().setPitch(0), 3000L);
    }
  }

  static public void sendMessage(String message) {
    if(noPlayer()) return;
    getPlayer().sendMessage(Text.literal(message), false);
  }

  static public void sendCommand(String command) {
    if(!command.startsWith("/")) command = "/" + command;
    getPlayer().networkHandler.sendCommand(command);
  }

  static public void lookAtYaw(@Nullable BlockPos targetPos) {
    if(targetPos == null || noPlayer()) return;
    BlockPos playerPos = getPlayer().getBlockPos();
    double deltaX = targetPos.getX() - playerPos.getX();
    double deltaZ = targetPos.getZ() - playerPos.getZ();

    // Calculate the angle in radians
    double angle = Math.atan2(deltaZ, deltaX);

    // Convert the angle to degrees
    double yaw = Math.toDegrees(angle) - 90; // Subtract 90 to adjust for Minecraft's yaw convention

    // Set the player's yaw
    getPlayer().setHeadYaw((float) yaw);
    getPlayer().setBodyYaw((float) yaw);
    getPlayer().setYaw((float) yaw);
  }

  static public void lookAtPitch(@Nullable BlockPos targetPos) {
    if(targetPos == null || noPlayer()) return;
    BlockPos playerPos = getPlayer().getBlockPos();
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
    getPlayer().setPitch((float) pitch);
  }

  static public void lookAt(@Nullable BlockPos targetPos) {
    lookAtYaw(targetPos);
    lookAtPitch(targetPos);
  }

  static public float directionToYaw(@Nullable Direction direction) {
    if (direction == null) return noPlayer() ? 0.0f : getPlayer().getYaw();
    switch (direction) {
      case NORTH -> { return 180.0f; }
      case SOUTH -> { return 0.0f; }
      case WEST -> { return 90.0f; }
      case EAST -> { return -90.0f; }
      default -> { return noPlayer() ? 0.0f : getPlayer().getYaw(); }
    }
  }
  static public float directionToPitch(@Nullable Direction direction) {
    if (direction == null) return noPlayer() ? 0.0f : getPlayer().getPitch();
    switch (direction) {
      case UP -> { return -90.0f; }
      case DOWN -> { return 90.0f; }
      default -> { return noPlayer() ? 0.0f : getPlayer().getPitch(); }
    }
  }

  static public void lookAt(@Nullable Direction direction) {
    if (direction == null || noPlayer()) return;
    float yaw = directionToYaw(direction);
    float pitch = directionToPitch(direction);
    getPlayer().setHeadYaw(yaw);
    getPlayer().setBodyYaw(yaw);
    getPlayer().setYaw(yaw);
    getPlayer().setPitch(pitch);
  }

  /**
   * Uses current player's yaw to look left.
   */
  static public void lookLeft() {
    if(noPlayer()) return;
    Direction direction = getPlayer().getHorizontalFacing();
    switch (direction) {
      case NORTH -> lookAt(Direction.WEST);
      case SOUTH -> lookAt(Direction.EAST);
      case WEST -> lookAt(Direction.SOUTH);
      case EAST -> lookAt(Direction.NORTH);
    }
  }

  static public void moveTo(@Nullable BlockPos targetPos, boolean... sprint) {
    if (targetPos == null || noPlayer()) return;
    getPlayer().setSprinting(sprint.length > 0 && sprint[0]);

    BlockPos playerPos = getPlayer().getBlockPos();
    double deltaX = targetPos.getX() - playerPos.getX();
    double deltaY = targetPos.getY() - playerPos.getY();
    double deltaZ = targetPos.getZ() - playerPos.getZ();

    Vec3d movementVector = new Vec3d(deltaX, deltaY, deltaZ);

    // Limit the movement vector to a maximum length of 3 blocks
    double maxDistance = getPlayer().getMovementSpeed()*3;
    if (movementVector.length() > maxDistance) {
      movementVector = movementVector.normalize().multiply(maxDistance);
    }

    getPlayer().move(MovementType.SELF, movementVector);
  }

}
