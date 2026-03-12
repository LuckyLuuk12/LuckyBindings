package nl.kablan.luckybindings.path;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import nl.kablan.luckybindings.config.option.BlockOption;
import nl.kablan.luckybindings.config.option.BooleanOption;
import nl.kablan.luckybindings.config.option.ConfigOption;
import nl.kablan.luckybindings.config.option.EnumOption;
import nl.kablan.luckybindings.config.option.IntegerOption;
import nl.kablan.luckybindings.config.option.ListOption;
import nl.kablan.luckybindings.config.option.StringOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared path planning helpers used by follow/highlight actions.
 */
public final class PathModePlanner {
    public static final String MODE_PLAYER = "PLAYER";
    public static final String MODE_LOCATION = "LOCATION";
    public static final String MODE_BLOCKS = "BLOCKS";

    public static final String OPT_PATH_MODE = "Path Mode";
    public static final String OPT_TARGET_PLAYER = "Target Player";
    public static final String OPT_TARGET_X = "Target X";
    public static final String OPT_TARGET_Y = "Target Y";
    public static final String OPT_TARGET_Z = "Target Z";
    public static final String OPT_BLOCKS_TO_FOLLOW = "Blocks to Follow";
    public static final String OPT_USE_VANILLA_VALIDATION = "Use Vanilla Validation";

    private PathModePlanner() {
    }

    public static Result computePath(Minecraft client, LocalPlayer player, List<ConfigOption<?>> arguments) {
        if (client.level == null) {
            return Result.failure("World is not loaded yet.");
        }

        String mode = getPathMode(arguments);
        BlockPos origin = player.blockPosition().below();

        return switch (mode) {
            case MODE_PLAYER -> computePlayerPath(client, origin, arguments);
            case MODE_LOCATION -> computeLocationPath(client, origin, arguments);
            case MODE_BLOCKS -> computeBlocksPath(client, player, origin, arguments);
            default -> Result.failure("Unsupported path mode: " + mode);
        };
    }

    public static String getPathMode(List<ConfigOption<?>> arguments) {
        ConfigOption<?> option = findOption(arguments, OPT_PATH_MODE);
        if (option instanceof EnumOption enumOption) {
            String value = enumOption.getValue();
            if (MODE_PLAYER.equals(value) || MODE_LOCATION.equals(value) || MODE_BLOCKS.equals(value)) {
                return value;
            }
        }
        return MODE_BLOCKS;
    }

    public static int getInt(List<ConfigOption<?>> arguments, String name, int fallback) {
        ConfigOption<?> option = findOption(arguments, name);
        if (option instanceof IntegerOption integerOption) {
            return integerOption.getValue();
        }
        if (option instanceof StringOption stringOption) {
            try {
                return Integer.parseInt(stringOption.getValue());
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    public static boolean getBoolean(List<ConfigOption<?>> arguments, String name, boolean fallback) {
        ConfigOption<?> option = findOption(arguments, name);
        if (option instanceof BooleanOption booleanOption) {
            return booleanOption.getValue();
        }
        return fallback;
    }

    private static Result computePlayerPath(Minecraft client, BlockPos origin, List<ConfigOption<?>> arguments) {
        String targetName = getString(arguments, OPT_TARGET_PLAYER, "").trim();
        if (targetName.isEmpty()) {
            return Result.failure("Target Player is empty.");
        }

        Player target = client.level.players().stream()
            .filter(player -> player.getName().getString().equalsIgnoreCase(targetName))
            .findFirst()
            .orElse(null);
        if (target == null) {
            return Result.failure("Player '" + targetName + "' is not online/in range.");
        }

        List<BlockPos> path = computeVanillaPath(client, origin, target.blockPosition());
        if (path.isEmpty()) {
            return Result.failure("Vanilla pathfinder could not reach player '" + targetName + "'.");
        }
        return Result.success(path);
    }

    private static Result computeLocationPath(Minecraft client, BlockPos origin, List<ConfigOption<?>> arguments) {
        BlockPos target = BlockPos.containing(
            getInt(arguments, OPT_TARGET_X, origin.getX()),
            getInt(arguments, OPT_TARGET_Y, origin.getY()),
            getInt(arguments, OPT_TARGET_Z, origin.getZ())
        );

        List<BlockPos> path = computeVanillaPath(client, origin, target);
        if (path.isEmpty()) {
            return Result.failure("Vanilla pathfinder could not reach the target location.");
        }
        return Result.success(path);
    }

    private static Result computeBlocksPath(Minecraft client, LocalPlayer player, BlockPos origin, List<ConfigOption<?>> arguments) {
        List<Block> targetBlocks = getTargetBlocks(arguments);
        if (targetBlocks.isEmpty()) {
            return Result.failure("No valid blocks configured in Blocks to Follow.");
        }

        PathComputationConfig config = new PathComputationConfig(
            getInt(arguments, "Max Radius", PathComputationConfig.DEFAULT.maxRadius()),
            getInt(arguments, "Max Rounds", PathComputationConfig.DEFAULT.maxRounds()),
            getInt(arguments, "Max Gap Size", PathComputationConfig.DEFAULT.maxGapSize()),
            getInt(arguments, "Max Nodes", PathComputationConfig.DEFAULT.maxNodes())
        );

        DeterministicPathPlanner planner = new DeterministicPathPlanner(
            client.level,
            origin,
            targetBlocks,
            config,
            player.getYRot()
        );

        List<BlockPos> computed = planner.findPath();
        if (computed.isEmpty()) {
            return Result.failure("No deterministic block path found.");
        }

        if (!getBoolean(arguments, OPT_USE_VANILLA_VALIDATION, true)) {
            return Result.success(computed);
        }

        List<BlockPos> vanilla = computeVanillaPath(client, origin, computed.get(computed.size() - 1));
        if (!vanilla.isEmpty()) {
            return Result.success(vanilla);
        }

        return Result.success(computed);
    }

    private static List<BlockPos> computeVanillaPath(Minecraft client, BlockPos origin, BlockPos target) {
        if (client.level == null) {
            return List.of();
        }

        Zombie probe = new Zombie(EntityType.ZOMBIE, client.level);
        probe.setPos(origin.getX() + 0.5D, origin.getY(), origin.getZ() + 0.5D);

        PathNavigation navigation = probe.getNavigation();
        Path path = navigation.createPath(target, 0);
        if (path == null || path.getNodeCount() == 0) {
            return List.of();
        }

        List<BlockPos> result = new ArrayList<>(path.getNodeCount());
        for (int i = 0; i < path.getNodeCount(); i++) {
            result.add(path.getNodePos(i));
        }
        return result;
    }

    private static String getString(List<ConfigOption<?>> arguments, String name, String fallback) {
        ConfigOption<?> option = findOption(arguments, name);
        if (option instanceof StringOption stringOption) {
            return stringOption.getValue();
        }
        return fallback;
    }

    private static List<Block> getTargetBlocks(List<ConfigOption<?>> arguments) {
        ConfigOption<?> option = findOption(arguments, OPT_BLOCKS_TO_FOLLOW);
        if (!(option instanceof ListOption<?> listOption)) {
            return List.of();
        }

        List<Block> blocks = new ArrayList<>();
        for (int i = 0; i < listOption.size(); i++) {
            ConfigOption<?> item = listOption.getItem(i);
            if (item instanceof BlockOption blockOption && blockOption.isValidBlock()) {
                Block block = blockOption.getBlock();
                if (block != Blocks.AIR && !blocks.contains(block)) {
                    blocks.add(block);
                }
            }
        }
        return blocks;
    }

    private static ConfigOption<?> findOption(List<ConfigOption<?>> arguments, String name) {
        for (ConfigOption<?> option : arguments) {
            if (name.equals(option.getName())) {
                return option;
            }
        }
        return null;
    }

    public record Result(List<BlockPos> nodes, String error) {
        public static Result success(List<BlockPos> nodes) {
            return new Result(nodes, null);
        }

        public static Result failure(String error) {
            return new Result(List.of(), error);
        }

        public boolean ok() {
            return error == null;
        }
    }
}