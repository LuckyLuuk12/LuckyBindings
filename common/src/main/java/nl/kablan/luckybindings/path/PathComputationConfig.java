package nl.kablan.luckybindings.path;

public record PathComputationConfig(
    int maxRadius,
    int maxRounds,
    int maxGapSize,
    int maxNodes
) {
    public static final PathComputationConfig DEFAULT = new PathComputationConfig(64, 2, 1, 4096);
}