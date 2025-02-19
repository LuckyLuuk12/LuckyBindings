package me.luckyluuk.luckybindings.actions;

import me.luckyluuk.luckybindings.model.Player;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class FollowBlock extends Action {
  private Block block;
  private int maxSearchDistance = 3;
  private boolean sprint = false;
  static private boolean isActivated = false;
  public FollowBlock(String... args) {
    super("follow_block", """
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
  public void execute(@Nullable Player p) {
    if (p == null || block == null) return;
    p.sendMessage("Following Block: " + block.getTranslationKey());
    BlockPos targetPos = findClosestBlock(p);
    p.sendMessage("Target Block: " + targetPos + " Your Position: " + p.getBlockPos());
    if(targetPos == null) return;
    isActivated = !isActivated;
    while(isActivated) {
      p.lookAtYaw(targetPos);
      p.moveTo(targetPos, sprint);
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
  private BlockPos findClosestBlock(@NotNull Player p) {
    World world = p.getWorld();
    Vec3d playerPos = p.getMinecraftClient().player.getPos();
    Direction direction = p.getHorizontalFacing();

    for (int i = 1; i <= maxSearchDistance; i++) {
      for (int j = -maxSearchDistance; j <= maxSearchDistance; j++) {
        for (int k = -maxSearchDistance; k <= maxSearchDistance; k++) {
          BlockPos targetPos = getPosByOffset(playerPos, direction, i, k, j);
          p.sendMessage("Checking:" + targetPos.getX() + ", " + targetPos.getY() + ", " + targetPos.getZ() + " with i=" + i + ", k=" + k + ", j=" + j);
          if (world.getBlockState(targetPos).getBlock() == block) {
            return targetPos;
          }
        }
      }
    }
    return null; // Return null if no block is found within the search distance
  }

  private BlockPos getPosByOffset(Vec3d playerPos, Direction direction, int xOffset, int yOffset, int zOffset) {
    Vec3d res = switch(direction) {
      case EAST, SOUTH -> playerPos.add(xOffset, yOffset, zOffset);
      case WEST -> playerPos.add(-xOffset, yOffset, zOffset);
      case NORTH -> playerPos.add(xOffset, yOffset, -zOffset);
      default -> throw new IllegalArgumentException("Unexpected value: " + direction);
    };
    return new BlockPos(new Vec3i((int) res.x, (int) res.y, (int) res.z));
  }

}
