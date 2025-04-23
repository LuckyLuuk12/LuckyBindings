package me.luckyluuk.luckybindings.model;

import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PathMiddleFinder {

  private final World world;
  private final PlayerEntity player;
  private final Block targetBlock;
  private final BlockPos origin;
  private final int maxRadius;
  private final int maxRounds;

  private final Map<BlockPos, Integer> densityMap = new HashMap<>();

  public PathMiddleFinder(World world, PlayerEntity player, Block targetBlock, int... options) {
    this.world = world;
    this.player = player;
    this.targetBlock = targetBlock;
    this.maxRadius = options.length > 0 ? options[0] : 64;
    this.maxRounds = options.length > 1 ? options[1] : 2;
    this.origin = player.getBlockPos().down();
  }
  public PathMiddleFinder(ClientPlayerEntity player, Block block, Collection<Integer> options) {
    this(player.getWorld(), player, block, options.stream().mapToInt(i -> i).toArray());
  }

  public List<BlockPos> findPath() {
    buildConnectedInitialMap();
    Deque<Map<BlockPos, Integer>> history = new ArrayDeque<>();
    history.add(new HashMap<>(densityMap)); // Save the initial state
    int iteration = 0;
    while (iteration <= maxRounds && dfs(origin, new HashSet<>(), new ArrayList<>(), origin)) {
      // Save the current state before sharpening
      if (history.size() == 2) history.pollFirst(); // Keep only the last two states
      history.add(new HashMap<>(densityMap));

      // Apply density sharpening
      Map<BlockPos, Integer> tempDensityMap = applyDensitySharpening(densityMap);
      densityMap.clear();
      densityMap.putAll(tempDensityMap);
      iteration++;
    }

    // Revert to the last valid state if no cycle is found
    if (!dfs(origin, new HashSet<>(), new ArrayList<>(), origin) && !history.isEmpty()) {
      densityMap.clear();
      densityMap.putAll(Objects.requireNonNull(history.pollLast()));
    }

    return extractBestPath();
  }

  public void debugPath(@NotNull List<BlockPos> path) {
    for (int i = 0; i < path.size(); i++) {
      BlockPos pos = path.get(i);
      float hue = i / (float) path.size();
      int rgb = Color.HSBtoRGB(hue, 1.0f, 1.0f);
      DustParticleEffect particle = new DustParticleEffect(rgb, 1.0f);
      world.addParticle(particle, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 0, 0, 0);
    }
  }

  private void buildConnectedInitialMap() {
    Set<BlockPos> visited = new HashSet<>();
    Queue<BlockPos> queue = new ArrayDeque<>();
    queue.add(origin);

    while (!queue.isEmpty()) {
      BlockPos current = queue.poll();
      if (!visited.add(current)) continue;
      if (current.getManhattanDistance(origin) > maxRadius) continue;
      if (world.getBlockState(current).getBlock() != targetBlock) continue;

      densityMap.put(current, getNeighbors(current, true).size());
      // Find neighbors without using the density map as it is not yet built
      queue.addAll(getNeighbors(current, true));
    }
  }

  private Map<BlockPos, Integer> applyDensitySharpening(Map<BlockPos, Integer> originalDensityMap) {
    Map<BlockPos, Integer> newDensityMap = new HashMap<>();

    // Step 1: Add density values to neighbors
    for (Map.Entry<BlockPos, Integer> entry : originalDensityMap.entrySet()) {
      BlockPos current = entry.getKey();
      int density = entry.getValue();

      // Add the current block's density to its neighbors
      for (BlockPos neighbor : getNeighbors(current)) {
        newDensityMap.put(neighbor, newDensityMap.getOrDefault(neighbor, 0) + density);
      }
    }

    // Step 2: Remove blocks with density <= 0
    newDensityMap.entrySet().removeIf(entry -> entry.getValue() <= 0);

    return newDensityMap;
  }

  private List<BlockPos> extractBestPath() {
    // First Dijkstra run: Find the furthest node from the origin
    List<BlockPos> pathToFurthest = dijkstra(origin, true);
    if (pathToFurthest.isEmpty()) return pathToFurthest;

    // Remove blocks in the first path from the density map
    for (BlockPos pos : pathToFurthest) {
      densityMap.remove(pos);
    }

    BlockPos furthestNode = pathToFurthest.getLast();

    // Second Dijkstra run: Find the path back to the origin
    List<BlockPos> pathToOrigin = dijkstra(furthestNode, false);
    if (pathToOrigin.isEmpty()) return pathToFurthest;

    // Combine the two paths to form a cycle
    pathToOrigin.removeFirst(); // Remove duplicate origin node
    pathToFurthest.addAll(pathToOrigin);

    return pathToFurthest;
  }

  private boolean dfs(BlockPos current, Set<BlockPos> visited, List<BlockPos> path, BlockPos origin) {
    if (!densityMap.containsKey(current)) return false;

    // Add the current block to the visited set and path
    visited.add(current);
    path.add(current);

    // Check if we have returned to the origin and formed a cycle
    if (current.equals(origin) && path.size() > 1) {
      return true;
    }

    // Sort neighbors by density (descending) to prioritize denser paths
    List<BlockPos> neighbors = getNeighbors(current);
    neighbors.sort(Comparator.comparingInt((BlockPos bp) -> -densityMap.getOrDefault(bp, 0)));

    for (BlockPos neighbor : neighbors) {
      if (!visited.contains(neighbor) || (neighbor.equals(origin) && path.size() > 1)) {
        if (dfs(neighbor, visited, path, origin)) {
          return true;
        }
      }
    }

    // Backtrack if no cycle is found
    path.removeLast();
    return false;
  }

  private List<BlockPos> dijkstra(BlockPos start, boolean forward) {
    float playerYaw = player.getYaw();
    Map<BlockPos, Integer> distances = new HashMap<>();
    Map<BlockPos, BlockPos> previous = new HashMap<>();
    PriorityQueue<BlockPos> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

    for (BlockPos pos : densityMap.keySet()) {
      distances.put(pos, Integer.MAX_VALUE);
    }
    distances.put(start, 0);
    queue.add(start);

    int maxDensity = densityMap.values().stream().max(Integer::compareTo).orElse(Integer.MAX_VALUE);

    while (!queue.isEmpty()) {
      BlockPos current = queue.poll();
      List<BlockPos> neighbors = getNeighbors(current);

      // Sort neighbors based on the sortByYaw flag
      neighbors.sort(Comparator
        .comparingInt((BlockPos neighbor) -> -densityMap.getOrDefault(neighbor, 0)) // Higher density first
        .thenComparingDouble(neighbor ->
          forward
            ? yawDifference(playerYaw, calculateYaw(current, neighbor)) // Closest yaw
            : -yawDifference(playerYaw, calculateYaw(current, neighbor)) // Farthest yaw
        )
      );

      for (BlockPos neighbor : neighbors) {
        int weight = maxDensity + 1 - densityMap.getOrDefault(neighbor, 0); // Prefer dense blocks
        int fullPathLength = distances.get(current) + weight;
        if (fullPathLength < distances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
          distances.put(neighbor, fullPathLength);
          previous.put(neighbor, current);
          queue.add(neighbor);
        }
      }
    }

    // Find the furthest reachable node, or the origin if no nodes are reachable. Or the origin if the path is backwards
    BlockPos end = forward ? distances.entrySet().stream()
      .filter(e -> previous.containsKey(e.getKey()))
      .max(Comparator.comparingInt(Map.Entry::getValue))
      .map(Map.Entry::getKey)
      .orElse(start) : origin;

    // Reconstruct the path
    List<BlockPos> path = new LinkedList<>();
    for (BlockPos at = end; at != null; at = previous.get(at)) {
      path.addFirst(at);
    }
    return path;
  }

  private double yawDifference(float playerYaw, float targetYaw) {
    float diff = Math.abs(playerYaw - targetYaw) % 360;
    return Math.min(diff, 360 - diff); // Return the smallest angle difference
  }

  private float calculateYaw(BlockPos from, BlockPos to) {
    double dx = to.getX() - from.getX();
    double dz = to.getZ() - from.getZ();
    return (float) Math.toDegrees(Math.atan2(dz, dx)) - 90; // Convert to Minecraft yaw
  }


  private List<BlockPos> getNeighbors(BlockPos pos, boolean... notUseDensityMap) {
    // use the density map by default unless notUseDensityMap is set to true
    boolean useDensityMapCheck = notUseDensityMap.length == 0 || !notUseDensityMap[0];
    List<BlockPos> neighbors = new ArrayList<>();
    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        if (dx == 0 && dz == 0) continue;
        BlockPos neighbor = pos.add(dx, 0, dz);
        if(useDensityMapCheck && densityMap.containsKey(neighbor)) neighbors.add(neighbor);
        else if(world.getBlockState(neighbor).getBlock() == targetBlock) neighbors.add(neighbor);
      }
    }
    return neighbors;
  }
}
