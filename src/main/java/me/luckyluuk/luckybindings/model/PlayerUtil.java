package me.luckyluuk.luckybindings.model;

import lombok.Getter;
import me.luckyluuk.luckybindings.handlers.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EntityView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
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
        Scheduler.runLater(ignored -> getPlayer().setHeadYaw(centerYaw - yawIncrement * (index + 1)), i * 60L / (step * 4L));
        Scheduler.runLater(ignored -> getPlayer().setHeadYaw(centerYaw + yawIncrement * (index + 1)), (i + step) * 60L / (step * 4L));
      }
      Scheduler.runLater(ignored -> getPlayer().setHeadYaw(centerYaw), 60L);
    } else {
      getPlayer().setPitch(0);
      for (int i = 0; i < step; i++) {
        Scheduler.runLater(ignored -> getPlayer().setPitch(-15), i * 60L / (step * 4L));
        Scheduler.runLater(ignored -> getPlayer().setPitch(15), (i + step) * 60L / (step * 4L));
      }
      Scheduler.runLater(ignored -> getPlayer().setPitch(0), 60L);
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
    if (targetPos == null || noPlayer()) return;
    double d = targetPos.getX() + 0.5 - getPlayer().getX();
    double f = targetPos.getZ() + 0.5 - getPlayer().getZ();
//    getPlayer().lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, target);
    double yaw = Math.atan2(f, d) * 180.0 / Math.PI - 90.0;
    getPlayer().setYaw((float) yaw);
//    getPlayer().setHeadYaw((float) yaw);
//    getPlayer().setBodyYaw((float) yaw);
//    getPlayer().prevYaw = (float) yaw;
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

  static public void lookRight() {
    if(noPlayer()) return;
    Direction direction = getPlayer().getHorizontalFacing();
    switch (direction) {
      case NORTH -> lookAt(Direction.EAST);
      case SOUTH -> lookAt(Direction.WEST);
      case WEST -> lookAt(Direction.NORTH);
      case EAST -> lookAt(Direction.SOUTH);
    }
  }

//  static public void moveTo(@Nullable BlockPos targetPos, boolean... sprint) {
//    if (targetPos == null || noPlayer()) return;
//    getPlayer().setSprinting(sprint.length > 0 && sprint[0]);
//
//    BlockPos playerPos = getPlayer().getBlockPos();
//    double deltaX = targetPos.getX() - playerPos.getX();
//    double deltaY = targetPos.getY() - playerPos.getY();
//    double deltaZ = targetPos.getZ() - playerPos.getZ();
//
//    Vec3d movementVector = new Vec3d(deltaX, deltaY, deltaZ);
//
//    // Limit the movement vector to a maximum length of 3 blocks
//    double maxDistance = getPlayer().getMovementSpeed()*3;
//    if (movementVector.length() > maxDistance) {
//      movementVector = movementVector.normalize().multiply(maxDistance);
//    }
//
//    getPlayer().move(MovementType.SELF, movementVector);
//  }

  static public CompletableFuture<Boolean> moveTo(@Nullable BlockPos targetPos, int maxSteps, boolean... sprint) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();
    if (targetPos == null || noPlayer()) {
      future.completeExceptionally(new IllegalArgumentException("Target position is null or player is unavailable."));
      return future;
    }

    ClientPlayerEntity player = getPlayer();
    player.setSprinting(sprint.length > 0 && sprint[0]);

    AtomicInteger steps = new AtomicInteger(0);

    Scheduler.runRepeatedly(task -> {
      if (steps.incrementAndGet() > maxSteps) {
        player.setVelocity(Vec3d.ZERO); // stop movement
        future.completeExceptionally(new IllegalStateException("Max steps exceeded."));
        task.cancel(true);
        return;
      }

      Vec3d playerPos = player.getPos();
      Vec3d targetVec = new Vec3d(targetPos.getX() + 0.5, playerPos.y, targetPos.getZ() + 0.5);
      Vec3d direction = targetVec.subtract(playerPos);

      if (direction.lengthSquared() < 0.25) { // close enough to target
        player.setVelocity(Vec3d.ZERO); // stop movement
        future.complete(true);
        task.cancel(true);
        return;
      }

      direction = direction.normalize();

      // Set velocity toward the target
      double speed = player.getMovementSpeed();
      if (player.isSprinting()) {
        speed *= 1.3;
      }
      Vec3d velocity = direction.multiply(speed * 2.5); // tune as needed
      player.setVelocity(velocity);
//      player.velocityModified = true;
    }, 0L);

    return future;
  }


//  static public void moveTo(@Nullable BlockPos targetPos, boolean... sprint) {
//    if (targetPos == null || noPlayer()) return;
//
//    ClientPlayerEntity player = getPlayer();
//    player.setSprinting(sprint.length > 0 && sprint[0]);
//
//    // Calculate the direction vector to the target position
//    Vec3d playerPos = player.getPos();
//    Vec3d targetVec = new Vec3d(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5); // Center of the block
//    Vec3d direction = targetVec.subtract(playerPos).normalize();
//
//    // Calculate the movement speed (walking or sprinting)
//    double speed = player.getMovementSpeed();
//    if (player.isSprinting()) {
//      speed *= 1.3; // Sprinting multiplier
//    }
//
//    // Calculate the movement vector for this tick
//    Vec3d movementVector = direction.multiply(speed / 20.0); // Divide by 20 ticks per second
//
//    // Apply the movement vector using the game's physics
//    player.move(MovementType.SELF, movementVector);
//  }

  static public ArrayList<PlayerEntity> getPlayersInRange(double range) {
    if(noPlayer() || isNoPlayerInRange(range)) return new ArrayList<>();
    return new ArrayList<>(getPlayer().getWorld().getEntitiesByClass(PlayerEntity.class, getPlayer().getBoundingBox().expand(range), player -> player != getPlayer()));
  }

  @Nullable
  static public PlayerEntity getClosestPlayer(double maxDistance) {
    if(noPlayer() || isNoPlayerInRange(maxDistance)) return null;
    double d = -1.0;
    PlayerEntity playerEntity = null;
    EntityView entityView = getPlayer().getWorld();
    for(PlayerEntity playerEntity2 : entityView.getPlayers()) {
      if(playerEntity2 == getPlayer()) continue;
      double e = playerEntity2.squaredDistanceTo(getPlayer());
      if((maxDistance < 0.0 || e < maxDistance * maxDistance) && (d == -1.0 || e < d)) {
        d = e;
        playerEntity = playerEntity2;
      }
    }

    return playerEntity;
  }
  // TODO: Somehow these methods don't detect SPECTATORS, issue lies in the getPlayers() method I think
  static public boolean isNoPlayerInRange(double range) {
    if(noPlayer()) return true;
    EntityView entityView = getPlayer().getWorld();
    for(PlayerEntity playerEntity2 : entityView.getPlayers()) {
      if(playerEntity2 == getPlayer()) continue;
      double e = playerEntity2.squaredDistanceTo(getPlayer());
      if(range < 0.0 || e < range * range) return false;
    }
    return true;
  }

  public static CompletableFuture<Boolean> walkPath(Queue<BlockPos> path, AtomicBoolean isActivated, boolean sprint) {
    CompletableFuture<Boolean> future = new CompletableFuture<>();

    if (path == null || path.isEmpty() || !isActivated.get() || PlayerUtil.noPlayer()) {
      future.complete(false);
      return future;
    }

    ClientPlayerEntity player = PlayerUtil.getPlayer();
    player.setSprinting(sprint);
    AtomicReference<BlockPos> lastPos = new AtomicReference<>(null);
    Scheduler.runRepeatedly(task -> {
      if (!isActivated.get() || path.isEmpty()) {
        MinecraftClient.getInstance().options.forwardKey.setPressed(false); // Stop walking
        task.cancel(true);
        future.complete(path.isEmpty()); // Complete if path is empty
        return;
      }
//      double distance = player.getPos().squaredDistanceTo(path.peek().getX() + 0.5, player.getY(), path.peek().getZ() + 0.5);
//      BlockPos nextPos = path.poll();
//      if (nextPos == null) {
//        MinecraftClient.getInstance().options.forwardKey.setPressed(false); // Stop walking
//        task.cancel(true);
//        future.complete(false);
//        return;
//      }
//
//      PlayerUtil.lookAtYaw(nextPos.up()); // Look at the next position
//      MinecraftClient.getInstance().options.forwardKey.setPressed(true); // Start walking
//
//      Vec3d playerPos = player.getPos();
//      Vec3d targetVec = new Vec3d(nextPos.getX() + 0.5, playerPos.y, nextPos.getZ() + 0.5);
//      if (playerPos.squaredDistanceTo(targetVec) < 0.25) { // Close enough to the target
//        MinecraftClient.getInstance().options.forwardKey.setPressed(false); // Stop walking
//      }
      // First check if lastPos is null or the distance between lastPos and current getPlayer()'s pos is less than 0.15, if so, poll the next position
      BlockPos next = lastPos.get() == null || lastPos.get().getSquaredDistance(player.getPos()) < 0.5 ? path.poll() : lastPos.get();
      // If next is null, stop walking
      if (next == null) {
        MinecraftClient.getInstance().options.forwardKey.setPressed(false); // Stop walking
        task.cancel(true);
        future.complete(true);
        return;
      }
      // If next is not null, set lastPos to next
      lastPos.set(next);
      // Look at the next position
      lookAtYaw(next.up());
      // Force forward-key to be pressed
      MinecraftClient.getInstance().options.forwardKey.setPressed(true);
    }, 1L, 1L); // Adjust delay as needed

    return future;
  }
}
