package me.luckyluuk.luckybindings.model;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PathEdgeFinder {

  private final World world;
  private final Block targetBlock;
  private final int maxRadius;
  private final BlockPos origin;
  private final int maxGap;
  private boolean[][] matrix;
  private final int center;

  public PathEdgeFinder(World world, PlayerEntity player, Block targetBlock, int... options) {
    this.world = world;
    this.targetBlock = targetBlock;
    this.maxRadius = options.length > 0 ? options[0] : 20;
    this.maxGap = options.length > 1 ? options[1] : 0; // Default to no gaps
    this.origin = player.getBlockPos().down();
    this.center = maxRadius;
  }

  /**
   * Debug the path by adding particles to the world.
   * @param path The path to debug / show
   */
  public void debugPath(@NotNull List<BlockPos> path) {
    float hue = 0.0f; // Start with red (hue = 0.0)
    float hueStep = 1.0f / path.size(); // Step through the rainbow
    float saturation = 1.0f; // Keep saturation constant
    float brightness = 1.0f; // Start with full brightness

    for (int i = 0; i < path.size(); i++) {
      BlockPos pos = path.get(i);

      // Adjust hue and brightness dynamically
      hue = (hue + hueStep) % 1.0f; // Cycle through hues
      brightness = 0.7f + 0.3f * (float) Math.cos(i * 0.1); // Oscillate brightness

      // Convert HSB to RGB
      int rgb = java.awt.Color.HSBtoRGB(hue, saturation, brightness);

      // Create the DustParticleEffect with the RGB color
      DustParticleEffect particle = new DustParticleEffect(rgb, 1.0f);

      // Add the particle to the world
      world.addParticle(particle,
        pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
        0, 0, 0);
    }
  }

  public List<BlockPos> findPath() {
    generateMatrix();
    Set<BlockPos> edgeBlocks = findEdgeBlocks();
    List<BlockPos> connectedEdge = getConnectedBlocks(edgeBlocks, origin);
    return sortPathFromOrigin(connectedEdge);
  }

  private void generateMatrix() {
    int size = maxRadius * 2 + 1;
    matrix = new boolean[size][size];
    for (int dx = -maxRadius; dx <= maxRadius; dx++) {
      for (int dz = -maxRadius; dz <= maxRadius; dz++) {
        BlockPos pos = origin.add(dx, 0, dz);
        matrix[dx + center][dz + center] = world.getBlockState(pos).getBlock() == targetBlock;
      }
    }
  }

  private Set<BlockPos> findEdgeBlocks() {
    Set<BlockPos> edges = new HashSet<>();
    int size = matrix.length;
    for (int x = 0; x < size; x++) {
      for (int z = 0; z < size; z++) {
        if (!matrix[x][z]) continue;
        for (int dx = -1; dx <= 1; dx++) {
          for (int dz = -1; dz <= 1; dz++) {
            if (dx == 0 && dz == 0) continue;
            int nx = x + dx;
            int nz = z + dz;
            if (nx < 0 || nz < 0 || nx >= size || nz >= size || !matrix[nx][nz]) {
              edges.add(new BlockPos(origin.getX() + x - center, origin.getY(), origin.getZ() + z - center));
              break;
            }
          }
        }
      }
    }
    return edges;
  }

  private List<BlockPos> getConnectedBlocks(Set<BlockPos> blocks, BlockPos start) {
    List<BlockPos> connected = new ArrayList<>();
    Set<BlockPos> visited = new HashSet<>();
    Queue<BlockPos> queue = new ArrayDeque<>();

    // Find nearest block in the set to start from
    BlockPos nearest = blocks.stream().min(Comparator.comparingInt(start::getManhattanDistance)).orElse(start);
    queue.add(nearest);
    visited.add(nearest);

    while (!queue.isEmpty()) {
      BlockPos current = queue.poll();
      connected.add(current);

      for (int dx = -1; dx <= 1; dx++) {
        for (int dz = -1; dz <= 1; dz++) {
          if (Math.abs(dx) + Math.abs(dz) == 0 || Math.abs(dx) + Math.abs(dz) > maxGap + 1) continue;
          BlockPos neighbor = current.add(dx, 0, dz);
          if (blocks.contains(neighbor) && !visited.contains(neighbor)) {
            queue.add(neighbor);
            visited.add(neighbor);
          }
        }
      }
    }
    return connected;
  }

  private List<BlockPos> sortPathFromOrigin(List<BlockPos> blocks) {
    List<BlockPos> sorted = new ArrayList<>();
    Set<BlockPos> visited = new HashSet<>();
    BlockPos current = blocks.stream().min(Comparator.comparingInt(origin::getManhattanDistance)).orElse(origin);
    sorted.add(current);
    visited.add(current);

    while (sorted.size() < blocks.size()) {
      BlockPos finalCurrent = current;
      Optional<BlockPos> next = blocks.stream()
        .filter(p -> !visited.contains(p))
        .min(Comparator.comparingInt(p -> p.getManhattanDistance(finalCurrent)));

      if (next.isEmpty()) break;
      current = next.get();
      sorted.add(current);
      visited.add(current);
    }

    return sorted;
  }
}
