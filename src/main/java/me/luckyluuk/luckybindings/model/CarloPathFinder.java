package me.luckyluuk.luckybindings.model;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CarloPathFinder {

  private final World world;
  private final Block targetBlock;
  private final int maxSearchRadius;
  private final int simulationRounds;
  private final Random random = new Random();

  private boolean[][] blockMatrix;
  private final BlockPos origin;

  public CarloPathFinder(World world, PlayerEntity player, Block targetBlock, int... options) {
    this.world = world;
    this.targetBlock = targetBlock;
    this.maxSearchRadius = options.length > 0 ? options[0] : 289;
    this.simulationRounds = options.length > 1 ? options[1] : 300;
    this.origin = player.getBlockPos().down();
  }

  public List<BlockPos> findPath() {
    generateBlockMatrix();
    return runMonteCarloSimulation(this.simulationRounds);
  }

  /**
   * Generate a 2D matrix of blocks around the origin position.
   */
  private void generateBlockMatrix() {
    int size = maxSearchRadius * 2 + 1;
    blockMatrix = new boolean[size][size];

    for (int dx = -maxSearchRadius; dx <= maxSearchRadius; dx++) {
      for (int dz = -maxSearchRadius; dz <= maxSearchRadius; dz++) {
        BlockPos pos = origin.add(dx, 0, dz);
        blockMatrix[dx + maxSearchRadius][dz + maxSearchRadius] =
          world.getBlockState(pos).getBlock() == targetBlock;
      }
    }
  }

  /**
   * Run the Monte Carlo simulation to find the best path.
   * @param attempts The number of random walks to perform
   * @return The best path found evaluated by {@link #evaluatePath(List)}
   */
  private List<BlockPos> runMonteCarloSimulation(int attempts) {
    List<BlockPos> bestPath = new ArrayList<>();
    int bestScore = 0;

    for (int i = 0; i < attempts; i++) {
      List<BlockPos> path = randomWalk();
      int score = evaluatePath(path);
      if (score > bestScore) {
        bestScore = score;
        bestPath = path;
      }
    }

    return bestPath;
  }

  /**
   * Perform a random walk from the origin position.
   * @return A list of BlockPos positions representing the path
   */
  @NotNull
  private List<BlockPos> randomWalk() {
    List<BlockPos> path = new ArrayList<>();
    Set<BlockPos> visited = new HashSet<>();

    BlockPos current = origin;
    path.add(current);
    visited.add(current);

    // Perform a random walk for a limited number of steps
    for (int i = 0; i < maxSearchRadius; i++) {
      // Randomly select a direction (this is the core of randomness)
      List<BlockPos> neighbors = getValidNeighbors(current, visited);
      if (neighbors.isEmpty()) break;

      // Randomly choose one of the valid neighbors
      current = neighbors.get(random.nextInt(neighbors.size()));

      // Add the new position to the path and mark it as visited
      path.add(current);
      visited.add(current);
    }

    return path;
  }

  /**
   * Get the valid neighbors of a block position limited by maxSearchRadius and on a 2D plane.
   * @param pos The block position to get the neighbors of
   * @param visited The set of visited block positions
   * @return A list of valid neighbors: only those that are within the search radius and not visited
   */
  @NotNull
  private List<BlockPos> getValidNeighbors(@NotNull BlockPos pos, @NotNull Set<@NotNull BlockPos> visited) {
    List<BlockPos> neighbors = new ArrayList<>();
    int[][] offsets = {
      {1, 0}, {-1, 0}, {0, 1}, {0, -1},
      {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
    };

    for (int[] offset : offsets) {
      BlockPos neighbor = pos.add(offset[0], 0, offset[1]);
      int x = neighbor.getX() - origin.getX() + maxSearchRadius;
      int z = neighbor.getZ() - origin.getZ() + maxSearchRadius;

      if (x >= 0 && x < blockMatrix.length && z >= 0 && z < blockMatrix[0].length) {
        if (blockMatrix[x][z] && !visited.contains(neighbor)) {
          neighbors.add(neighbor);
        }
      }
    }

    return neighbors;
  }

  private int evaluatePath(@NotNull List<BlockPos> path) {
    if (path.size() < 7) return 0;

    BlockPos start = path.getFirst();
    BlockPos end = path.getLast();
    double distToStart = start.getManhattanDistance(end);

    int peakDist = 0;
    int peakIndex = 0;

    // Find the peak distance and peak index
    for (int i = 0; i < path.size(); i++) {
      int dist = start.getManhattanDistance(path.get(i));
      if (dist > peakDist) {
        peakDist = dist;
        peakIndex = i;
      }
    }

    int halfway = path.size() / 2;
    int maxOffset = 5; // allow 3-block deviation
    boolean isSymmetric = Math.abs(peakIndex - halfway) <= maxOffset;
    boolean returnedClose = distToStart < 3;

    int diagonalPenalty = 0;
    for (int i = 1; i < path.size(); i++) {
      int dx = Math.abs(path.get(i).getX() - path.get(i - 1).getX());
      int dz = Math.abs(path.get(i).getZ() - path.get(i - 1).getZ());
      if (dx == 1 && dz == 1) {
        diagonalPenalty += 1;
      }
    }

    // Penalize if any block in the path has more than 2 neighbors, using getNeighbors method
    int neighborPenalty = 0;
    for (BlockPos pos : path) {
      if (getNeighbors(pos, path).size() > 2) {
        neighborPenalty += 1;
      }
    }

    // Final score calculation
    return path.size() * 10
      + peakDist * peakDist
      + (isSymmetric ? 1500 : 0)
      + (returnedClose ? 2000 : -100) // Big bonus for closing the loop
      - (diagonalPenalty * 5) // Tune this multiplier
      - (Math.abs(peakIndex - halfway) > maxOffset ? 0 : 1000) // Penalize if the peak is close to the start
      - neighborPenalty; // Penalize for too many neighbors
  }

  /**
   * Debug the path by adding particles to the world.
   * @param path The path to debug / show
   */
  public void debugPath(@NotNull List<BlockPos> path) {
    float hue = 0.0f; // Start with red (hue = 0.0)
    float hueStep = 1.0f / path.size(); // Step through the rainbow

    for (BlockPos pos : path) {
      // Convert hue to RGB
      int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);

      // Create the DustParticleEffect with the RGB color
      DustParticleEffect particle = new DustParticleEffect(rgb, 1.0f);

      // Add the particle to the world
      world.addParticle(particle,
        pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
        0, 0, 0);

      // Increment the hue for the next color
      hue += hueStep;
    }
  }

  /**
   * Get the neighbors of a block position.
   * @param pos The block position to get the neighbors of
   * @param path The list of possible neighbors
   * @return A list of neighbors
   */
  private List<BlockPos> getNeighbors(BlockPos pos, List<BlockPos> path) {
//    return path.stream().filter(neighbor -> neighbor.isWithinDistance(pos, 1)).toList();
    List<BlockPos> neighbors = new ArrayList<>();
    int[][] offsets = {
      {1, 0}, {-1, 0}, {0, 1}, {0, -1},
      {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
    };
    for (int[] offset : offsets) {
      BlockPos neighbor = pos.add(offset[0], 0, offset[1]);
      if (path.contains(neighbor)) {
        neighbors.add(neighbor);
      }
    }
    return neighbors;
  }

}
