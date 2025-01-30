package me.luckyluuk.luckybindings.actions;

import me.luckyluuk.luckybindings.model.Player;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.stream.Stream;

public class PathHighlight extends Action {
  private final BlockPos target;
  private final int range;

  public PathHighlight(BlockPos target, int... range) {
    super("path_highlight");
    this.target = target;
    this.range = range.length > 0 ? range[0] : 128;
  }

  /**
   * Spawns particles to highlight the path to the target block.
   * @param p The player to spawn the particles for.
   */
  @Override
  public void execute(Player p) {
    if (p == null) return;
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
}
