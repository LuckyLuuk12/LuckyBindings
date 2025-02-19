package me.luckyluuk.luckybindings.actions;

import me.luckyluuk.luckybindings.handlers.Scheduler;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import static me.luckyluuk.luckybindings.model.PlayerUtil.*;


public class FollowBlock extends Action {
  private Block block;
  private int maxSearchDistance = 3;
  private boolean sprint = false;
  static private boolean isActivated = false;
  public FollowBlock(String... args) {
    super("follow_block", """
    WARNING: THIS MIGHT BE CONSIDERED AS CHEATING AND CAN GET YOU BANNED ON SOME SERVERS.
    Using this action will cause the player to start following the specified block.
    This means it will attempt to find the block in the direction the player is looking
    limited by the max search distance. This direction will be limited to the horizontal
    plane and to the North, South, East, or West directions.
    From the player's current location a square area will be searched for the block.
    The width and length of the square are determined by the max search distance.
    If the block is found, the player will start walking towards it.
    """);
    setArgs(args);
  }

  @Override
  public void execute() {
    ClientPlayerEntity pl = MinecraftClient.getInstance().player;
    if (pl == null || block == null) return;
    isActivated = !isActivated;
    try {
    final ScheduledFuture<?>[] future = new ScheduledFuture<?>[1];
    future[0] = Scheduler.runRepeatedly(() -> {
      if (!isActivated) {
        if(future[0] != null) future[0].cancel(true);
        return;
      }
      ClientPlayerEntity p = MinecraftClient.getInstance().player;
      if (p == null) return;
      BlockPos targetPos = findClosestBlock(p);
      // If no block is found, rotate left and continue searching
      if(targetPos == null) {
        lookLeft();
        return;
      }
      sendMessage(p.getBlockPos().getX() + ", " + p.getBlockPos().getY() + ", " + p.getBlockPos().getZ() + " > " + targetPos.getX() + ", " + targetPos.getY() + ", " + targetPos.getZ() + " | " + getPlayer().clientWorld.getBlockState(targetPos).getBlock().getTranslationKey());
      lookAtYaw(targetPos);
      moveTo(targetPos, sprint);
    }, 1L);
    } catch (Exception e) {
      sendMessage("An error occurred, stopping the action...");
      isActivated = false;
    }
  }

  @Override
  public void setArgs(String... args) {
    if (args.length > 0) {
      this.block = Registries.BLOCK.get(Identifier.of(args[0]));
    }
    this.sprint = args.length > 1 && Boolean.parseBoolean(args[1]);
    if (args.length > 2) this.maxSearchDistance = Integer.parseInt(args[2]);
  }

  /**
   * Makes a square area in front of the player to search for the block.
   * The width and length of the square are determined by the max search distance.
   * The search is limited to the horizontal plane and to the North, South, East, or West directions.
   * @param p The player to search from.
   * @return The position of the found block or null if no block is found.
   */
  @Nullable
  private BlockPos findClosestBlock(@NotNull ClientPlayerEntity p) {
    World world = p.getWorld();
    BlockPos playerPos = p.getBlockPos();
    Direction direction = p.getHorizontalFacing();
    Map<BlockPos, Integer> distanceMap = new HashMap<>();

    for (int i = 1; i <= maxSearchDistance; i++) {
      for (int j = -maxSearchDistance; j <= maxSearchDistance; j++) {
        for (int k = -maxSearchDistance; k <= maxSearchDistance; k++) {
          BlockPos targetPos = getPosByOffset(playerPos, direction, i, k, j);
          if (world.getBlockState(targetPos).getBlock() != block) continue;
          int distance = Math.abs(i) + Math.abs(j) + Math.abs(k);
          if (distance == 0 || sameLoc(playerPos, targetPos)) continue; // Skip if the distance is 0
          distanceMap.put(targetPos, distance);
        }
      }
    }

    return distanceMap.entrySet().stream()
      .min(Map.Entry.comparingByValue())
      .map(Map.Entry::getKey)
      .orElse(null); // Return null if no block is found within the search distance
  }

  private BlockPos getPosByOffset(BlockPos playerPos, Direction direction, int xOffset, int yOffset, int zOffset) {
    return switch (direction) {
      case EAST -> playerPos.add(xOffset, yOffset, zOffset);
      case WEST -> playerPos.add(-xOffset, yOffset, zOffset);
      case SOUTH -> playerPos.add(zOffset, yOffset, xOffset);
      case NORTH -> playerPos.add(zOffset, yOffset, -xOffset);
      default -> throw new IllegalArgumentException("Unexpected value: " + direction);
    };

  }

  private boolean sameLoc(BlockPos a, BlockPos b) {
    return a.getX() == b.getX() && a.getY() == b.getY() && a.getZ() == b.getZ();
  }

}
