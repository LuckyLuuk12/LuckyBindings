package nl.kablan.luckybindings.path;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * Deterministic path planner inspired by the old PathMiddleFinder logic.
 * It finds connected blocks of the same type around origin and computes a density-guided cycle-like path.
 */
public class DeterministicPathPlanner {
    private static final double TURN_PENALTY_SCALE = 0.08D;
    private static final double SHARP_TURN_EXTRA_PENALTY = 6.0D;
    private static final double ENDPOINT_DENSITY_RATIO = 0.45D;
    private static final int ENDPOINT_MIN_DENSE_NEIGHBORS = 2;

    private final Level level;
    private final BlockPos origin;
    private final List<Block> targetBlocks;
    private final PathComputationConfig config;
    private final float referenceYaw;

    private final Map<BlockPos, Integer> densityMap = new HashMap<>();

    public DeterministicPathPlanner(Level level, BlockPos origin, Block targetBlock, PathComputationConfig config, float referenceYaw) {
        this(level, origin, List.of(targetBlock), config, referenceYaw);
    }

    public DeterministicPathPlanner(Level level, BlockPos origin, List<Block> targetBlocks, PathComputationConfig config, float referenceYaw) {
        this.level = level;
        this.origin = origin;
        this.targetBlocks = new ArrayList<>(targetBlocks);
        this.config = config;
        this.referenceYaw = referenceYaw;
    }

    public List<BlockPos> findPath() {
        buildConnectedInitialMap();
        if (densityMap.isEmpty()) {
            return List.of();
        }

        Deque<Map<BlockPos, Integer>> history = new ArrayDeque<>();
        history.add(new HashMap<>(densityMap));

        int rounds = 0;
        while (rounds < config.maxRounds() && hasCycle()) {
            if (history.size() == 2) {
                history.pollFirst();
            }
            history.add(new HashMap<>(densityMap));
            applyDensitySharpening();
            rounds++;
        }

        if (!hasCycle() && !history.isEmpty()) {
            densityMap.clear();
            densityMap.putAll(history.getLast());
        }

        return extractBestPath();
    }

    private void buildConnectedInitialMap() {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new ArrayDeque<>();
        queue.add(origin);

        while (!queue.isEmpty() && densityMap.size() < config.maxNodes()) {
            BlockPos current = queue.poll();
            if (!visited.add(current)) {
                continue;
            }
            if (current.distManhattan(origin) > config.maxRadius()) {
                continue;
            }
            if (!isTargetBlock(level.getBlockState(current).getBlock())) {
                continue;
            }

            List<BlockPos> neighbors = getNeighbors(current, false, true);
            densityMap.put(current, neighbors.size());
            queue.addAll(neighbors);
        }
    }

    private void applyDensitySharpening() {
        Map<BlockPos, Integer> sharpened = new HashMap<>();
        for (Map.Entry<BlockPos, Integer> entry : densityMap.entrySet()) {
            BlockPos current = entry.getKey();
            int density = entry.getValue();
            for (BlockPos neighbor : getNeighbors(current, true, false)) {
                sharpened.merge(neighbor, density, Integer::sum);
            }
        }
        sharpened.entrySet().removeIf(e -> e.getValue() <= 0);
        densityMap.clear();
        densityMap.putAll(sharpened);
    }

    private boolean hasCycle() {
        return dfs(origin, new HashSet<>(), new ArrayList<>());
    }

    private boolean dfs(BlockPos current, Set<BlockPos> visited, List<BlockPos> path) {
        if (!densityMap.containsKey(current)) {
            return false;
        }

        visited.add(current);
        path.add(current);

        if (current.equals(origin) && path.size() > 1) {
            return true;
        }

        List<BlockPos> neighbors = getNeighbors(current, true, false);
        neighbors.sort(Comparator
            .comparingInt((BlockPos pos) -> -densityMap.getOrDefault(pos, 0))
            .thenComparingInt(BlockPos::getX)
            .thenComparingInt(BlockPos::getZ));

        for (BlockPos next : neighbors) {
            if (!visited.contains(next) || (next.equals(origin) && path.size() > 1)) {
                if (dfs(next, visited, path)) {
                    return true;
                }
            }
        }

        path.removeLast();
        return false;
    }

    private List<BlockPos> extractBestPath() {
        DijkstraResult outwardSearch = dijkstra(origin, true);
        BlockPos end = selectOutwardEnd(outwardSearch);
        if (end == null) {
            return List.of();
        }

        List<BlockPos> outward = outwardSearch.buildPathTo(end);
        if (outward.isEmpty()) {
            return outward;
        }

        for (BlockPos pos : outward) {
            densityMap.remove(pos);
        }

        DijkstraResult backwardSearch = dijkstra(end, false);
        List<BlockPos> backward = backwardSearch.buildPathTo(origin);
        if (backward.isEmpty()) {
            return outward;
        }
        backward.removeFirst();

        List<BlockPos> full = new ArrayList<>(outward.size() + backward.size());
        full.addAll(outward);
        full.addAll(backward);
        return full;
    }

    private DijkstraResult dijkstra(BlockPos start, boolean forward) {
        Map<BlockPos, Double> distances = new HashMap<>();
        Map<BlockPos, BlockPos> previous = new HashMap<>();
        PriorityQueue<BlockPos> queue = new PriorityQueue<>(
            Comparator.comparingDouble((BlockPos pos) -> distances.getOrDefault(pos, Double.MAX_VALUE))
                .thenComparingInt((BlockPos pos) -> pos.getX())
                .thenComparingInt((BlockPos pos) -> pos.getZ())
        );

        for (BlockPos pos : densityMap.keySet()) {
            distances.put(pos, Double.MAX_VALUE);
        }
        distances.put(start, 0.0D);
        queue.add(start);

        int maxDensity = densityMap.values().stream().max(Integer::compareTo).orElse(1);

        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            double currentDistance = distances.getOrDefault(current, Double.MAX_VALUE);
            if (currentDistance == Double.MAX_VALUE) {
                continue;
            }

            List<BlockPos> neighbors = getNeighbors(current, true, false);
            neighbors.sort(Comparator
                .comparingInt((BlockPos pos) -> -densityMap.getOrDefault(pos, 0))
                .thenComparingDouble(pos -> {
                    float yaw = calculateYaw(current, pos);
                    double diff = yawDifference(referenceYaw, yaw);
                    return forward ? diff : -diff;
                })
                .thenComparingInt(BlockPos::getX)
                .thenComparingInt(BlockPos::getZ));

            for (BlockPos neighbor : neighbors) {
                double candidate = currentDistance + edgeCost(current, neighbor, previous, maxDensity, forward);
                if (candidate < distances.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    distances.put(neighbor, candidate);
                    previous.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }
        return new DijkstraResult(start, distances, previous);
    }

    private BlockPos selectOutwardEnd(DijkstraResult outwardSearch) {
        if (outwardSearch.previous().isEmpty()) {
            return null;
        }

        int maxDensity = densityMap.values().stream().max(Integer::compareTo).orElse(1);
        int densityThreshold = Math.max(1, (int) Math.ceil(maxDensity * ENDPOINT_DENSITY_RATIO));

        Comparator<Map.Entry<BlockPos, Double>> scoreComparator = Comparator
            .comparingDouble(this::endpointScore)
            .thenComparingInt(e -> e.getKey().getX())
            .thenComparingInt(e -> e.getKey().getZ());

        BlockPos strict = outwardSearch.distances().entrySet().stream()
            .filter(e -> outwardSearch.previous().containsKey(e.getKey()))
            .filter(e -> densityMap.getOrDefault(e.getKey(), 0) >= densityThreshold)
            .filter(e -> countDenseNeighbors(e.getKey(), densityThreshold) >= ENDPOINT_MIN_DENSE_NEIGHBORS)
            .max(scoreComparator)
            .map(Map.Entry::getKey)
            .orElse(null);
        if (strict != null) {
            return strict;
        }

        BlockPos relaxed = outwardSearch.distances().entrySet().stream()
            .filter(e -> outwardSearch.previous().containsKey(e.getKey()))
            .filter(e -> densityMap.getOrDefault(e.getKey(), 0) >= densityThreshold)
            .max(scoreComparator)
            .map(Map.Entry::getKey)
            .orElse(null);
        if (relaxed != null) {
            return relaxed;
        }

        return outwardSearch.distances().entrySet().stream()
            .filter(e -> outwardSearch.previous().containsKey(e.getKey()))
            .max(scoreComparator)
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    private double endpointScore(Map.Entry<BlockPos, Double> entry) {
        BlockPos pos = entry.getKey();
        double travelCost = entry.getValue();
        int density = densityMap.getOrDefault(pos, 0);
        double yawBias = yawDifference(referenceYaw, calculateYaw(origin, pos));

        // Prefer far and dense endpoints, while softly preferring forward-facing ones.
        return travelCost + density * 0.35D - yawBias * 0.04D;
    }

    private int countDenseNeighbors(BlockPos pos, int densityThreshold) {
        int count = 0;
        for (BlockPos neighbor : getNeighbors(pos, true, false)) {
            if (densityMap.getOrDefault(neighbor, 0) >= densityThreshold) {
                count++;
            }
        }
        return count;
    }

    private double edgeCost(BlockPos current, BlockPos neighbor, Map<BlockPos, BlockPos> previous, int maxDensity, boolean forward) {
        int densityCost = maxDensity + 1 - densityMap.getOrDefault(neighbor, 0);
        float heading = currentHeading(current, previous, forward);
        float stepYaw = calculateYaw(current, neighbor);
        double turn = yawDifference(heading, stepYaw);

        double turnPenalty = turn * TURN_PENALTY_SCALE;
        if (turn > 150.0D) {
            turnPenalty += SHARP_TURN_EXTRA_PENALTY;
        }
        return densityCost + turnPenalty;
    }

    private float currentHeading(BlockPos current, Map<BlockPos, BlockPos> previous, boolean forward) {
        BlockPos parent = previous.get(current);
        if (parent != null) {
            return calculateYaw(parent, current);
        }
        return forward ? referenceYaw : wrapYaw(referenceYaw + 180.0F);
    }

    private static float wrapYaw(float yaw) {
        float wrapped = yaw % 360.0F;
        return wrapped < 0.0F ? wrapped + 360.0F : wrapped;
    }

    private record DijkstraResult(
        BlockPos start,
        Map<BlockPos, Double> distances,
        Map<BlockPos, BlockPos> previous
    ) {
        List<BlockPos> buildPathTo(BlockPos end) {
            if (end == null) {
                return List.of();
            }

            LinkedList<BlockPos> path = new LinkedList<>();
            for (BlockPos at = end; at != null; at = previous.get(at)) {
                path.addFirst(at);
                if (at.equals(start)) {
                    return path;
                }
            }
            return List.of();
        }
    }

    private static double yawDifference(float a, float b) {
        float diff = Math.abs(a - b) % 360.0F;
        return Math.min(diff, 360.0F - diff);
    }

    private static float calculateYaw(BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
    }

    private List<BlockPos> getNeighbors(BlockPos pos, boolean useDensityMap, boolean requireTargetBlock) {
        List<BlockPos> neighbors = new ArrayList<>();
        int gap = 1;
        while (neighbors.isEmpty() && gap <= config.maxGapSize()) {
            for (int dx = -gap; dx <= gap; dx++) {
                for (int dz = -gap; dz <= gap; dz++) {
                    if (dx == 0 && dz == 0) {
                        continue;
                    }
                    BlockPos neighbor = pos.offset(dx, 0, dz);
                    if (neighbor.distManhattan(origin) > config.maxRadius()) {
                        continue;
                    }

                    if (useDensityMap) {
                        if (densityMap.containsKey(neighbor)) {
                            neighbors.add(neighbor);
                        }
                    } else if (requireTargetBlock) {
                        if (isTargetBlock(level.getBlockState(neighbor).getBlock())) {
                            neighbors.add(neighbor);
                        }
                    } else {
                        neighbors.add(neighbor);
                    }
                }
            }
            gap++;
        }
        return neighbors;
    }

    private boolean isTargetBlock(Block block) {
        return targetBlocks.contains(block);
    }
}