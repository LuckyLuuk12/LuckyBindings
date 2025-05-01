package me.luckyluuk.luckybindings.actions;

import me.luckyluuk.luckybindings.handlers.Scheduler;
import me.luckyluuk.luckybindings.model.PathMiddleFinder;
import me.luckyluuk.luckybindings.model.PlayerUtil;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FollowBlock extends Action {
  private Block block;
  private boolean sprint = false;
  private final Collection<Integer> options = new ArrayList<>();

  private final AtomicBoolean isActivated = new AtomicBoolean(false);
  private Queue<BlockPos> path = new LinkedList<>();

  public FollowBlock(String... args) {
    super("follow_block", """
        WARNING: THIS MIGHT BE CONSIDERED AS CHEATING AND CAN GET YOU BANNED ON SOME SERVERS.

        Using this action will cause the player to start following the specified block.

        PARAMETERS:
        1. The block to follow (required).
        2. Whether to sprint while following the block (optional, default is false).
        3. The maximum search distance (optional, default is 64).
        4. The amount of path-sharpening rounds (optional, default is 2).
        5. The maximum gap between blocks in the path (optional, default is 1).
        6. The amount of steps taken to go from block to block (optional, default is 500).
        7. The distance your player must have from other players before stopping (optional, default is 32).
           This is to prevent detection by staff or other players.
        8. Whether to display the path with particles values <= 0 mean false (optional, default is false).
        """);
    setArgs(args);
  }

  @Override
  public void execute() {

    ClientPlayerEntity player = MinecraftClient.getInstance().player;
    if (player == null || block == null) return;
//    isActivated = !isActivated;
    isActivated.set(!isActivated.get());
    if (!isActivated.get()) {
      path.clear();
      return;
    }
    // Find the most optimal path to walk the shape of the pathway consisting of the target block
    PathMiddleFinder pmf = new PathMiddleFinder(player, block, options);
    path = new LinkedList<>(pmf.findPath());
    walk(player, path, pmf);
  }

  @Override
  public void setArgs(String... args) {
    if (args.length > 0) {
      this.block = Registries.BLOCK.get(Identifier.of(args[0]));
    }
    this.sprint = args.length > 1 && Boolean.parseBoolean(args[1]);
    for (int i = 2; i < args.length; i++) options.add(Integer.parseInt(args[i]));
  }

  private void walk(ClientPlayerEntity player, Queue<BlockPos> path, PathMiddleFinder pmf) {
    Queue<BlockPos> runningPath = new LinkedList<>(path);
    // If the path is empty, stop the action and notify the player
    if (path.isEmpty()) {
      player.sendMessage(Text.literal("No path found!"), false);
      isActivated.set(false);
      return;
    }
    player.sendMessage(Text.literal("Path found with " + runningPath.size() + " blocks."), false);
    // Debug / Display the path
    Scheduler.runRepeatedly(task -> {
      if(!isActivated.get()) {
        task.cancel(true);
        return;
      }
      if(path.isEmpty() || options.size() > 5 && options.stream().skip(5).findFirst().orElse(0) <= 0) {
        return;
      }
      pmf.debugPath(path.stream().toList());
    }, 2L, 0L);
    // Start walking the path
    PlayerUtil.walkPath(runningPath, isActivated, sprint).thenAccept(b -> {
      if(b) {
        walk(player, path, pmf);
      }
    }).exceptionally(ex -> {
      player.sendMessage(Text.literal("Error while walking path: " + ex.getMessage()), false);
      isActivated.set(false);
      return null;
    });
  }
}