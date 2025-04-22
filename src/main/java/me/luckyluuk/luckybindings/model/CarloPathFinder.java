package me.luckyluuk.luckybindings.model;

import me.luckyluuk.luckybindings.handlers.Scheduler;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

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
  public CompletableFuture<List<BlockPos>> findPath() {
    CompletableFuture<List<BlockPos>> future = new CompletableFuture<>();
    Scheduler.runLater(task -> {
      generateBlockMatrix();
      future.complete(runMonteCarloSimulation(this.simulationRounds));
    }, 0L);
    return future;
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
      List<BlockPos> path = i == 0 ? randomWalk() : randomModify(bestPath);
      int score = evaluatePath(path);
      if (score > bestScore) {
        bestScore = score;
        bestPath = path;
      }
    }

    return bestPath;
  }



  /**
   * Randomly modify an existing path by selecting a random segment and modifying it.
   */
  @NotNull
  private List<BlockPos> randomModify(@NotNull List<BlockPos> path) {
    List<BlockPos> newPath = new ArrayList<>(path);
    if(newPath.isEmpty()) return randomWalk();
    // random action to either add, remove or modify to the path
    int action = random.nextInt(100);
    // Randomly modify a position in the path to one of its neighbors
    if (action <= 40 && newPath.size() > 2) {
      int index = random.nextInt(1, newPath.size());
      BlockPos pos = newPath.get(index);
      List<BlockPos> neighbors = getValidNeighbors(pos, new HashSet<>(newPath));
      if (!neighbors.isEmpty()) {
        BlockPos newPos = neighbors.get(random.nextInt(neighbors.size()));
        int i = 0;
        while(newPath.contains(newPos) && i < neighbors.size()*2) {
          newPos = neighbors.get(random.nextInt(neighbors.size()));
          i++;
        }
        newPath.set(index, newPos);
      }
    }
    // Randomly add a new position somewhere in the path
    else {
      int index = newPath.size() <= 1 ? 0 : random.nextInt(1, newPath.size());
      BlockPos pos = newPath.get(index);
      List<BlockPos> neighbors = getValidNeighbors(pos, new HashSet<>(newPath));
      if(!neighbors.isEmpty()) {
        BlockPos newPos = neighbors.get(random.nextInt(neighbors.size()));
        int i = 0;
        while(newPath.contains(newPos) && i < neighbors.size()*2) {
          newPos = neighbors.get(random.nextInt(neighbors.size()));
          i++;
        }
        newPath.add(index, newPos);
      }
    }
    // Randomly remove the end of the path to allow shrinking, don't try it if the path is too small
//    else if (newPath.size() > 7) {
//      newPath.removeLast();
//    }
    return newPath;
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
  /**
   * Evaluate the path based on various criteria.
   * Beneficial properties:
   * <ul>
   *   <li>Length of the path</li>
   *   <li>Closed loop</li>
   *   <li>Symmetry</li>
   * </ul>
   * Penalized properties:
   * <ul>
   *   <li>Number of gaps</li>
   *   <li>Number of diagonals</li>
   *   <li>Number of backward steps</li>
   *   <li>Number of "more than 2 neighbors" in the path</li>
   * </ul>
   * @param path The path to evaluate
   * @return The score of the path
   */
  private int evaluatePath(@NotNull List<BlockPos> path) {
    if (path.size() < 7 || getGaps(path) > 1) return 0;
    // Final score calculation
//    return Math.floorDiv(
//      (int) (
//        Math.pow(1.25, path.size()) +
//          (isClosedLoop(path) ? 30 : 0) +
//          (isSymmetric(path) ? 5 : 0) +
//          getAverageDistanceFromOrigin(path) // reward for staying away from origin
//      ),
//        (getDiagonals(path) * 5) +
//        (getBackwardStepCount(path) * 4) +
//        (getDoubleNeighborCount(path) * 12) + // increased penalty
//        (getTurnCount(path) * 2) + // penalize sharp turns
//        1 // prevent divide by zero
//    );
    return path.size() -  getDoubleNeighborCount(path) * 2;
  }



  /**
   * Get the total number of "more than 2 neighbors" in the path.
   * @param path The path to check
   * @return The number of BlockPos in the path that have more than 2 neighbors
   */
  private int getDoubleNeighborCount(List<BlockPos> path) {
    int count = 0;
    for (BlockPos pos : path) {
      List<BlockPos> neighbors = new ArrayList<>();
      int[][] offsets = {
        {1, 0}, {-1, 0}, {0, 1}, {0, -1},
        {1, 1}, {-1, -1}, {1, -1}, {-1, 1}
      };
      for (int[] offset : offsets) {
        if(path.contains( pos.add(offset[0], 0, offset[1]))) neighbors.add(pos.add(offset[0], 0, offset[1]));
      }
      if (neighbors.size() > 2) count++;
    }
    return count;
  }
  /**
   * Get the number of diagonals in the path.
   * @param path The path to check
   * @return The number of diagonals in the path
   */
  private int getDiagonals(List<BlockPos> path) {
    int diagonals = 0;
    for (int i = 1; i < path.size(); i++) {
      int dx = Math.abs(path.get(i).getX() - path.get(i - 1).getX());
      int dz = Math.abs(path.get(i).getZ() - path.get(i - 1).getZ());
      if (dx == 1 && dz == 1) {
        diagonals++;
      }
    }
    return diagonals;
  }
  /**
   * Get the number of gaps in the path, meaning the number of times the path skips a block.
   * Either by going diagonally or by skipping a block in the x or z direction.
   * @param path The path to check
   * @return The number of gaps in the path
   */
  private int getGaps(List<BlockPos> path) {
    int gaps = 0;
    for (int i = 1; i < path.size(); i++) {
      int dx = Math.abs(path.get(i).getX() - path.get(i - 1).getX());
      int dz = Math.abs(path.get(i).getZ() - path.get(i - 1).getZ());
      if (dx > 1 || dz > 1) {
        gaps++;
      }
    }
    return gaps;
  }
  /**
   * Check if the path is a closed loop.
   * @param path The path to check
   * @param maxDistance The maximum distance between the start and end points to consider it a closed loop
   * @return True if the path is a closed loop, false otherwise
   */
  private boolean isClosedLoop(List<BlockPos> path, int... maxDistance) {
    if (path.size() < 2) return false;
    int maxDist = maxDistance.length > 0 ? maxDistance[0] : 3;
    BlockPos start = path.getFirst();
    BlockPos end = path.getLast();
    return start.getManhattanDistance(end) <= maxDist;
  }
  /**
   * Check if the path is symmetric, meaning that the distance from the start to the peak is equal to the distance from the peak to the end.
   * @param path The path to check
   * @param acceptedDeviation Optional accepted deviation of the symmetry
   * @return True if the path is symmetric, false otherwise
   */
  private boolean isSymmetric(List<BlockPos> path, int... acceptedDeviation) {
    if (path.size() < 2) return false;
    int maxDeviation = acceptedDeviation.length > 0 ? acceptedDeviation[0] : 3;
    BlockPos start = path.getFirst();
    BlockPos end = path.getLast();
    int peakIndex = path.size() / 2;
    int peakDist = getPeakDistance(path);
    int distToStart = start.getManhattanDistance(end);
    return Math.abs(peakIndex - distToStart) <= maxDeviation && peakDist > distToStart;
  }
  /**
   * Get the distance from the start to the peak of the path.
   * This is the maximum distance from the start to any point in the path.
   * @param path The path to check
   * @return The distance from the start to the peak of the path
   */
  private int getPeakDistance(List<BlockPos> path) {
    if (path.size() < 2) return 0;
    BlockPos start = path.getFirst();
    int peakDist = 0;
    for (BlockPos pos : path) {
      int dist = start.getManhattanDistance(pos);
      if (dist > peakDist) {
        peakDist = dist;
      }
    }
    return peakDist;
  }
  /**
   * If there is a BlockPos in the path and the next BlockPos in the path is closer to the start than the current one, given that it is before the "peak" of the path.
   * Then it is a backward step.
   * After the peak, it would be a backward step if the next BlockPos is further away from the start than the current one.
   * @param path The path to check
   * @return The number of backward steps in the path
   */
  private int getBackwardStepCount(List<BlockPos> path) {
    if (path.size() < 2) return 0;
    BlockPos start = path.getFirst();
    int peakIndex = path.size() / 2;
    int backwardSteps = 0;

    for (int i = 1; i < path.size(); i++) {
      BlockPos current = path.get(i);
      BlockPos previous = path.get(i - 1);
      int currentDist = start.getManhattanDistance(current);
      int previousDist = start.getManhattanDistance(previous);

      if (i < peakIndex && currentDist < previousDist) {
        backwardSteps++;
      } else if (i >= peakIndex && currentDist > previousDist) {
        backwardSteps++;
      }
    }

    return backwardSteps;
  }
  /**
   * Get the average distance from the origin to all BlockPos in the path.
   * @param path The path to check
   * @return The average distance from the origin to all BlockPos in the path
   */
  private int getAverageDistanceFromOrigin(List<BlockPos> path) {
    return (int) path.stream().mapToInt(origin::getManhattanDistance).average().orElse(0);
  }
  /**
   * Get the number of turns in the path.
   * A turn is detected if the direction vector changes.
   * @param path The path to check
   * @return The number of turns in the path
   */
  private int getTurnCount(@NotNull List<BlockPos> path) {
    if (path.size() < 3) return 0;
    int turns = 0;

    for (int i = 2; i < path.size(); i++) {
      BlockPos prev = path.get(i - 2);
      BlockPos curr = path.get(i - 1);
      BlockPos next = path.get(i);

      int dx1 = curr.getX() - prev.getX();
      int dz1 = curr.getZ() - prev.getZ();
      int dx2 = next.getX() - curr.getX();
      int dz2 = next.getZ() - curr.getZ();

      // Turn is detected if direction vector changes
      if (dx1 != dx2 || dz1 != dz2) {
        turns++;
      }
    }

    return turns;
  }

}
