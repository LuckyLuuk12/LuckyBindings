package me.luckyluuk.luckybindings.actions;

import me.luckyluuk.luckybindings.handlers.Scheduler;
import me.luckyluuk.luckybindings.model.PathEdgeFinder;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.luckyluuk.luckybindings.model.PlayerUtil.*;

public class FollowBlock extends Action {
  private Block block;
  private boolean sprint = false;
  private int maxSearchDistance = 3;
  private int stopWhenNearbyDistance = 32;
  private int maxGaps = 0;
  private boolean isActivated = false;
  private Queue<BlockPos> path = new LinkedList<>();

  public FollowBlock(String... args) {
    super("follow_block", """
        WARNING: THIS MIGHT BE CONSIDERED AS CHEATING AND CAN GET YOU BANNED ON SOME SERVERS.

        Using this action will cause the player to start following the specified block.

        PARAMETERS:
        - The block to follow (required).
        - Whether to sprint while following the block (optional, default is false).
        - The maximum search distance (optional, default is 3).
        - The maximum gap size allowed in the path (optional, default is 0).
        - The distance your player must have from other players before stopping (optional, default is 32).
           This is to prevent detection by staff or other players.
        """);
    setArgs(args);
  }

  @Override
  public void execute() {
    ClientPlayerEntity player = MinecraftClient.getInstance().player;
    if (player == null || block == null) return;

    isActivated = !isActivated;
    if (!isActivated) {
      path.clear();
      return;
    }

    PathEdgeFinder pef = new PathEdgeFinder(player.getWorld(), player, block, maxSearchDistance, maxGaps);
    path = new LinkedList<>(pef.findPath());
    Queue<BlockPos> runningPath = new LinkedList<>(path);
    if (path.isEmpty()) {
      player.sendMessage(Text.literal("No path found!"), false);
      isActivated = false;
      return;
    }
    player.sendMessage(Text.literal("Path found with " + path.size() + " blocks."), false);
    Scheduler.runRepeatedly(task -> {
      if(!isActivated) {
        task.cancel(true);
        return;
      }
      if(path == null || path.isEmpty()) return;
      pef.debugPath(path.stream().toList());
    }, 2L, 0L);

      // Start walking through the cycle TODO: Make the movement "smoother", for some reason there is a large interval per step in the path
//    CompletableFuture.runAsync(() -> {
//      while (isActivated && !player.isRemoved() && player.isLoaded()) {
//        walkPath(new LinkedList<>(runningPath), player).join();
//      }
//      player.velocityModified = false;
//    });
  }

  @Override
  public void setArgs(String... args) {
    if (args.length > 0) {
      this.block = Registries.BLOCK.get(Identifier.of(args[0]));
    }
    this.sprint = args.length > 1 && Boolean.parseBoolean(args[1]);
    if (args.length > 2) this.maxSearchDistance = Integer.parseInt(args[2]);
    if (args.length > 3) this.maxGaps = Integer.parseInt(args[3]);
    if (args.length > 4) this.stopWhenNearbyDistance = Integer.parseInt(args[4]);
  }

  /**
   * Walks a Queue of BlockPos positions.
   * @param path The path to walk
   * @param player The player to walk
   * @return A CompletableFuture that completes when the path is finished
   */
  private CompletableFuture<Boolean> walkPath(Queue<BlockPos> path, ClientPlayerEntity player) {
    if (!isActivated) {
      return CompletableFuture.completedFuture(false);
    }

    if (path.isEmpty()) {
      // Refill the path and restart walking
      path = this.path;
      if (path.isEmpty()) {
        return CompletableFuture.completedFuture(false);
      }
    }

    BlockPos nextPos = path.poll();
    if (nextPos == null) {
      return CompletableFuture.completedFuture(false);
    }

    // Move to the next position and chain the next call
    Queue<BlockPos> finalPath = path;
    return moveTo(nextPos.up(), 500, sprint)
      .thenCompose(success -> {
        lookAtYaw(nextPos.up().up());
        return walkPath(finalPath, player);
      })
      .exceptionally(ex -> {
        player.sendMessage(Text.literal("Failed to move: " + ex), false);
        isActivated = false;
        return false;
      });
  }
}