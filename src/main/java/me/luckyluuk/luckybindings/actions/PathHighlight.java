package me.luckyluuk.luckybindings.actions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.stream.Stream;

import static me.luckyluuk.luckybindings.model.PlayerUtil.sendMessage;

public class PathHighlight extends Action {
  private BlockPos target;
  private Integer range;

  public PathHighlight(String... args) {
    super("path_highlight", """
    Highlights the path to a target block using particles.
    The target block is specified by its x, y, z coordinates.
    Optionally, a range can be specified to only highlight the path
    if the player is within that range.
    """);
    setArgs(args);
  }

  @Override
  public void setArgs(String... args) {
    try {
      target = new BlockPos(
        Integer.parseInt(args[0]),
        Integer.parseInt(args[1]),
        Integer.parseInt(args[2])
      );
    } catch (NumberFormatException ignored) {}
    range = args.length > 3 ? Integer.parseInt(args[3]) : null;
  }

  /**
   * Spawns particles to highlight the path to the target block.
   */
  @Override
  public void execute() {
    ClientPlayerEntity p = MinecraftClient.getInstance().player;
    if (p == null || target == null) return;
    // If range is specified, check if the player is within range
    if (range != null) {
      double distance = p.getPos().distanceTo(new Vec3d(target.getX(), target.getY(), target.getZ()));
      if (distance > range) return;
    }
    sendMessage("Highlighting path to: " + target.getX() + ", " + target.getY() + ", " + target.getZ());
    World world = p.getWorld();
    PathAwareEntity tempEntity = new PathAwareEntity(EntityType.ZOMBIE, world) {}; // Dummy mob
    tempEntity.setPosition(p.getX(), p.getY(), p.getZ());
    EntityNavigation navigator = tempEntity.getNavigation();
    Stream<BlockPos> targetStream = Stream.of(this.target);
    // Generate the path
    Path path = navigator.findPathToAny(targetStream, 1);
    if (path != null) {
      // Display the path using particles
      for (int i = 0; i < path.getLength(); i++) {
        BlockPos pos = path.getNode(i).getBlockPos();
        spawnParticle(pos);
      }
    }
  }

  private void spawnParticle(BlockPos pos) {
    if(MinecraftClient.getInstance().world == null) return;
    MinecraftClient.getInstance().world.addParticle(
      ParticleTypes.END_ROD,
      pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
      0, 0.05, 0
    );
  }

  @Override
  public String toString() {
    return "PathHighlight{" +
            "target=" + target +
            ", range=" + range +
            '}';
  }
}
