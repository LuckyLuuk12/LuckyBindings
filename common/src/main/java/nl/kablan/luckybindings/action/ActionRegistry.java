package nl.kablan.luckybindings.action;

import nl.kablan.luckybindings.config.option.BooleanOption;
import nl.kablan.luckybindings.config.option.StringOption;
import nl.kablan.luckybindings.config.option.IntegerOption;
import nl.kablan.luckybindings.config.option.BlockOption;
import nl.kablan.luckybindings.config.option.EnumOption;
import nl.kablan.luckybindings.config.option.ListOption;
import nl.kablan.luckybindings.path.PathModePlanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionRegistry {
    private static final Map<String, ActionType<?>> ACTIONS = new HashMap<>();

    public static final ActionType<NothingAction> NOTHING = register(new ActionType<>(
            "nothing",
            "Does nothing.",
            List.of(),
            (type, args) -> new NothingAction(type)
    ));

    public static final ActionType<ExecuteCommandAction> EXECUTE_COMMAND = register(new ActionType<>(
            "execute_command",
            "Executes a command as the player.",
            List.of(new StringOption("Command", "The command to execute", "", "Enter WITHOUT leading /: enter 'killall' or '/copy' for WorldEdit. The leading / will be stripped if present.")),
      ExecuteCommandAction::new
    ));

    public static final ActionType<PrepareChatAction> PREPARE_CHAT = register(new ActionType<>(
            "prepare_chat",
            "Opens the chat with pre-filled text.",
            List.of(
                    new StringOption("Text", "The text to pre-fill the chat with", ""),
                    new BooleanOption("Send Immediately", "If true, send the message right away instead of opening draft chat", false)
            ),
      PrepareChatAction::new
    ));

    public static final ActionType<NodHeadAction> NOD_HEAD = register(new ActionType<>(
            "nod_head",
            "Makes the player's head nod.",
            List.of(
                    new BooleanOption("Agree", "Whether to nod in agreement (true) or disagreement (false)", true),
                    new IntegerOption("Agree Duration Ticks", "Duration of agree animation in ticks", 26, 8, 120, "How long the nod takes (20 ticks = 1 second). Higher = slower animation."),
                    new IntegerOption("Disagree Duration Ticks", "Duration of disagree animation in ticks", 42, 8, 160, "How long the head shake takes (20 ticks = 1 second). Higher = slower animation."),
                    new IntegerOption("Agree Pitch Amplitude", "Pitch swing angle for agree animation", 14, 2, 45, "How far the head tips up/down during a nod (in degrees). Higher = bigger nod."),
                    new IntegerOption("Disagree Yaw Amplitude", "Yaw swing angle for disagree animation", 30, 4, 70, "How far the head swings left/right during disagreement (in degrees). Higher = wider shake."),
                    new IntegerOption("Agree Cycles", "How many nod cycles to perform", 2, 1, 6, "Number of up-down nods. Higher = more bobs during the animation."),
                    new IntegerOption("Disagree Cycles", "How many left-right cycles to perform", 2, 1, 6, "Number of left-right shakes. Higher = faster oscillation."),
                    new EnumOption("Easing", "Interpolation profile used for nod animation", "sine",
                            List.of("sine", "smoothstep", "linear"))
            ),
      NodHeadAction::new
    ));

    public static final ActionType<HighlightPathAction> HIGHLIGHT_PATH = register(new ActionType<>(
            "highlight_path",
            "Highlights a computed path and refreshes as you move.",
            List.of(
                    new EnumOption("Path Mode", "How the target path is computed", PathModePlanner.MODE_BLOCKS,
                            List.of(PathModePlanner.MODE_PLAYER, PathModePlanner.MODE_LOCATION, PathModePlanner.MODE_BLOCKS)),
                    new StringOption("Target Player", "Username to path toward when Path Mode=PLAYER", ""),
                    new IntegerOption("Target X", "X coordinate of the target block", 0, -30000000, 30000000),
                    new IntegerOption("Target Y", "Y coordinate of the target block", 0, -64, 320),
                    new IntegerOption("Target Z", "Z coordinate of the target block", 0, -30000000, 30000000),
                    new ListOption<>(
                            "Blocks to Follow",
                            "List of block types to follow when Path Mode=BLOCKS",
                            new ArrayList<>(List.of(new BlockOption("Block", "", "stone")))
                    ),
                    new BooleanOption("Use Vanilla Validation", "When true, validate/fallback with vanilla zombie pathfinding in BLOCKS mode", true),
                    new IntegerOption("Range", "Maximum distance to show path (-1 = infinite)", -1, -1, 1000),
                    new IntegerOption("Update Interval Ticks", "How often to recompute/render path (20 ticks = 1s)", 40, 5, 400),
                    new IntegerOption("Recompute Distance", "Recompute only after moving this many blocks", 2, 1, 16),
                    new IntegerOption("Max Render Nodes", "Particle node cap per refresh", 160, 8, 512),
                    new BooleanOption("Stop When Reached", "Automatically stop highlighting when within 1.5 blocks of the path endpoint", true),
                    new BooleanOption("Toggle Mode", "When enabled, pressing the key while highlighting stops it; when disabled, pressing always restarts with a fresh path (recommended for hold-to-repeat)", false)
            ),
      HighlightPathAction::new
    ));

    public static final ActionType<FollowPathAction> FOLLOW_PATH = register(new ActionType<>(
            "follow_path",
            "Follows a path toward a player, location, or block-based path.",
            List.of(
                    new EnumOption("Path Mode", "How the target path is computed", PathModePlanner.MODE_BLOCKS,
                            List.of(PathModePlanner.MODE_PLAYER, PathModePlanner.MODE_LOCATION, PathModePlanner.MODE_BLOCKS)),
                    new StringOption("Target Player", "Username to path toward when Path Mode=PLAYER", ""),
                    new IntegerOption("Target X", "X coordinate when Path Mode=LOCATION", 0, -30000000, 30000000),
                    new IntegerOption("Target Y", "Y coordinate when Path Mode=LOCATION", 0, -64, 320),
                    new IntegerOption("Target Z", "Z coordinate when Path Mode=LOCATION", 0, -30000000, 30000000),
                    new ListOption<>(
                        "Blocks to Follow",
                        "List of block types to follow when Path Mode=BLOCKS",
                        new ArrayList<>(List.of(new BlockOption("Block", "", "stone")))
                    ),
                    new BooleanOption("Use Vanilla Validation", "When true, validate/fallback with vanilla zombie pathfinding in BLOCKS mode", true),
                    new BooleanOption("Sprint", "Whether to sprint while following", false),
                    new IntegerOption("Max Radius", "Max search radius in blocks", 64, 8, 256),
                    new IntegerOption("Max Rounds", "Density sharpening rounds", 2, 0, 8),
                    new IntegerOption("Max Gap Size", "Max gap to bridge between path blocks", 1, 1, 4),
                    new IntegerOption("Max Nodes", "Max visited nodes to protect performance", 4096, 256, 20000)
            ),
      FollowPathAction::new
    ));

    public static <T extends Action> ActionType<T> register(ActionType<T> type) {
        ACTIONS.put(type.id(), type);
        return type;
    }

    public static ActionType<?> get(String id) {
        return ACTIONS.get(id);
    }

    public static Collection<ActionType<?>> getAll() {
        return ACTIONS.values();
    }
    
    public static void init() {
        // Just to trigger class loading and registration
    }
}