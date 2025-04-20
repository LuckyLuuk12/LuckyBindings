package me.luckyluuk.luckybindings.actions;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static me.luckyluuk.luckybindings.model.PlayerUtil.*;

public class FollowBlock extends Action {
  private Block block;
  private boolean sprint = false;
  private int maxSearchDistance = 3;
  private int stopWhenNearbyDistance = 32;
  private boolean searchRotation = true; // true = right, false = left
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
        - The distance your player must have from other players before stopping (optional, default is 32).
           This is to prevent detection by staff or other players.
        - If no block is found, the player will rotate left or right and continue searching (optional, default is right).
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

    path = findPath(player);
    if (path.isEmpty()) {
      player.sendMessage(Text.literal("No path found!"), false);
      isActivated = false;
      return;
    }

    player.sendMessage(Text.literal("Path found with " + path.size() + " blocks."), false);

    // Start walking through the path
//    walkPath(new LinkedList<>(path), player);
    CompletableFuture<Boolean> isWalking = CompletableFuture.completedFuture(isActivated);
    // Start the whole path again if the future is completed TODO: this does not infinitely loop/walk the path...
    isWalking.thenRun(() -> {
      if (isActivated) {
        player.sendMessage(Text.literal("Path completed, starting again..."), false);
        walkPath(new LinkedList<>(path), player, isWalking);
      }
    }).exceptionally(ex -> {
      player.sendMessage(Text.literal("Error while walking the path: " + ex), false);
      isActivated = false;
      return null;
    });
  }


  private CompletableFuture<Boolean> walkPath(Queue<BlockPos> path, ClientPlayerEntity player, CompletableFuture<Boolean> future) {
    if(!isActivated) {
      future.complete(false);
      return future;
    }
    if (path.isEmpty()) {
      future.complete(true);
      return future;
    }
    BlockPos nextPos = path.poll();
    if (nextPos != null) {
      moveTo(nextPos.up(), 50, sprint).thenRun(() -> {
        // Continue to the next position after reaching the current one
        walkPath(path, player, future);
      }).exceptionally(ex -> {
        player.sendMessage(Text.literal("Failed to move: " + ex), false);
        isActivated = false;
        future.completeExceptionally(ex);
        return null;
      });
    }
    return future;
  }

  @Override
  public void setArgs(String... args) {
    if (args.length > 0) {
      this.block = Registries.BLOCK.get(Identifier.of(args[0]));
    }
    this.sprint = args.length > 1 && Boolean.parseBoolean(args[1]);
    if (args.length > 2) this.maxSearchDistance = Integer.parseInt(args[2]);
    if (args.length > 3) this.stopWhenNearbyDistance = Integer.parseInt(args[3]);
    if (args.length > 4) this.searchRotation = "r".equals(args[4].substring(0, 1));
  }

  private Queue<BlockPos> findPath(@NotNull ClientPlayerEntity player) {
    World world = player.getWorld();
    BlockPos start = player.getBlockPos().down();
//    Queue<BlockPos> queue = new LinkedList<>();
//    Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
//    Set<BlockPos> visited = new HashSet<>();
    Queue<BlockPos> path = new LinkedList<>();

//    queue.add(start);
//    visited.add(start);

//    while (!queue.isEmpty() && path.size() < maxSearchDistance) {
      //
//      BlockPos current = queue.poll();
//
//      // Check if the current block matches the target block type
//      if (world.getBlockState(current).getBlock() == this.block) {
//        path.add(current);
//      }
//
//      // Get neighbors and process them
//      for (BlockPos neighbor : getNeighbors(current, player.getHorizontalFacing(), world)) {
//        if (!visited.contains(neighbor)
//          && start.isWithinDistance(neighbor, maxSearchDistance)) {
//          visited.add(neighbor); // Mark as visited
//          queue.add(neighbor);   // Add to the queue
//          cameFrom.put(neighbor, current); // Track the path
//        }
//      }
//    }
    // Find a complete path to either the start position or a "dead end", if a dead end is found, the full path in reverse will be appended to the path before returning
    boolean deadEnd = false;
    if (world.getBlockState(start).getBlock() == this.block) path.add(start);
    while (!deadEnd) {
      boolean found = false;
      for (BlockPos neighbor : getNeighbors(path.isEmpty() ? start  : new ArrayList<>(path).getLast(), player.getHorizontalFacing(), world, start)) {
        if (path.size() >= maxSearchDistance) {
          deadEnd = true;
          break;
        }
        if (world.getBlockState(neighbor).getBlock() == this.block && !path.contains(neighbor)) {
          path.add(neighbor);
          found = true;
          break;
        }
      }
      if(!found || path.size() <= 1) break; // If no neighbors were found, break the loop
    }
    // If a dead end was found, append the path in reverse order
    if (deadEnd) {
      List<BlockPos> reversedPath = new ArrayList<>(path);
      Collections.reverse(reversedPath);
      // Remove the first element from the reversed path to avoid duplication
      if (!reversedPath.isEmpty()) reversedPath.removeFirst();
      path.addAll(reversedPath);
    }
    return path;
  }

  private List<BlockPos> getNeighbors(BlockPos pos, Direction facing, World world, BlockPos start) {
    List<BlockPos> neighbors = new ArrayList<>();

    // Add the neighbor in the facing direction first
    neighbors.add(pos.offset(facing));
    // Add the remaining neighbors
    for (Direction direction : Direction.values()) {
      if (direction != facing) neighbors.add(pos.offset(direction));
    }
    // Add diagonal neighbors
    neighbors.addAll(List.of(
      pos.north().east(), pos.north().west(), pos.south().east(), pos.south().west(),
      pos.up().north(), pos.up().south(), pos.up().east(), pos.up().west(),
      pos.down().north(), pos.down().south(), pos.down().east(), pos.down().west(),
      pos.up().north().east(), pos.up().north().west(), pos.up().south().east(), pos.up().south().west(),
      pos.down().north().east(), pos.down().north().west(), pos.down().south().east(), pos.down().south().west()
    ));

    return neighbors.stream()
      .filter(neighbor -> world.getBlockState(neighbor).getBlock() == this.block)
      .sorted((bp1, bp2) -> Double.compare(start.getSquaredDistance(bp2), start.getSquaredDistance(bp1))) // Sort the neighbors based on their distance from the start position, farthest first
      .collect(Collectors.toList());
  }

  private Queue<BlockPos> reconstructPath(Map<BlockPos, BlockPos> cameFrom, BlockPos end) {
    LinkedList<BlockPos> path = new LinkedList<>();
    BlockPos current = end;
    while (current != null) {
      path.addFirst(current);
      current = cameFrom.get(current);
    }
    return new LinkedList<>(path);
  }
}