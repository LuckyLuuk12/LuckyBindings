package nl.kablan.luckybindings.action;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import nl.kablan.luckybindings.config.option.BooleanOption;
import nl.kablan.luckybindings.config.option.ConfigOption;
import nl.kablan.luckybindings.config.option.IntegerOption;
import nl.kablan.luckybindings.path.PathModePlanner;
import nl.kablan.luckybindings.util.Scheduler;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Highlights a computed path over connected blocks, recalculating at intervals.
 *
 * execute() always (re)starts the highlight so that hold-to-repeat and repeated
 * key presses cause a fresh path computation rather than toggling off.
 */
public class HighlightPathAction implements Action {
    private static final double STOP_REACH_DIST = 1.5;

    private final ActionType<HighlightPathAction> type;
    private final List<ConfigOption<?>> arguments;
    private final AtomicBoolean active = new AtomicBoolean(false);

    private volatile List<BlockPos> cachedPath = List.of();
    private volatile BlockPos lastComputeOrigin;
    private volatile ScheduledFuture<?> task;

    public HighlightPathAction(ActionType<HighlightPathAction> type, List<ConfigOption<?>> arguments) {
        this.type = type;
        this.arguments = arguments;
    }

    @Override
    public void execute(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;

        if (active.get()) {
            if (getBooleanArgument("Toggle Mode", false)) {
                // Toggle Mode: pressing while active stops the highlight.
                cancelTask();
                player.displayClientMessage(Component.literal("Path highlight stopped."), true);
                return;
            }
            // Default: restart with a fresh path computation (good for hold-to-repeat).
            cancelTask();
        }

        active.set(true);
        cachedPath = List.of();
        lastComputeOrigin = null;

        int intervalTicks = Math.max(5, getIntArgument("Update Interval Ticks", 40));
        task = Scheduler.runRepeatedly(
            self -> client.execute(() -> tickHighlight(client, self)),
            intervalTicks, 1L);
        player.displayClientMessage(Component.literal("Path highlight started."), true);
    }

    private void tickHighlight(Minecraft client, ScheduledFuture<?> self) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null || !active.get()) {
            cancelTask();
            self.cancel(false);
            return;
        }

        int range = getIntArgument("Range", -1);
        int recomputeMove = Math.max(1, getIntArgument("Recompute Distance", 2));
        int maxRenderNodes = Math.max(8, getIntArgument("Max Render Nodes", 160));

        // Range check (when applicable)
        if (range >= 0) {
            int targetX = PathModePlanner.getInt(arguments, PathModePlanner.OPT_TARGET_X, player.blockPosition().getX());
            int targetY = PathModePlanner.getInt(arguments, PathModePlanner.OPT_TARGET_Y, player.blockPosition().getY());
            int targetZ = PathModePlanner.getInt(arguments, PathModePlanner.OPT_TARGET_Z, player.blockPosition().getZ());
            double dist = Math.sqrt(
                Math.pow(player.getX() - targetX, 2) +
                Math.pow(player.getY() - targetY, 2) +
                Math.pow(player.getZ() - targetZ, 2));
            if (dist > range) return;
        }

        // Recompute path when player has moved far enough or cache is empty
        BlockPos origin = player.blockPosition().below();
        if (lastComputeOrigin == null
                || origin.distManhattan(lastComputeOrigin) >= recomputeMove
                || cachedPath.isEmpty()) {
            PathModePlanner.Result result = PathModePlanner.computePath(client, player, arguments);
            cachedPath = result.nodes();
            if (!result.ok() && result.error() != null && !result.error().isBlank()) {
                player.displayClientMessage(Component.literal(result.error()), true);
            }
            lastComputeOrigin = origin;
        }

        if (cachedPath.isEmpty()) return;

        // Render particles along the path
        int step = Math.max(1, cachedPath.size() / maxRenderNodes);
        for (int i = 0; i < cachedPath.size(); i += step) {
            BlockPos pos = cachedPath.get(i);
            client.level.addParticle(ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                0, 0.01, 0);
        }

        // Stop-when-reached: check distance to final path node
        if (getBooleanArgument("Stop When Reached", false)) {
            BlockPos last = cachedPath.getLast();
            double dx = player.getX() - (last.getX() + 0.5);
            double dz = player.getZ() - (last.getZ() + 0.5);
            if (Math.sqrt(dx * dx + dz * dz) < STOP_REACH_DIST) {
                cancelTask();
                self.cancel(false);
                player.displayClientMessage(Component.literal("Reached path destination."), true);
            }
        }
    }

    private void cancelTask() {
        active.set(false);
        ScheduledFuture<?> running = task;
        if (running != null) running.cancel(false);
        task = null;
        cachedPath = List.of();
        lastComputeOrigin = null;
    }

    @Override
    public ActionType<?> getType() { return type; }

    @Override
    public List<ConfigOption<?>> getArguments() { return arguments; }

    // isRunning() returns false: the scheduler runs independently; returning true
    // would incorrectly block sequential-mode ordering for subsequent actions.
    @Override
    public boolean isRunning() { return false; }

    // ── helpers ───────────────────────────────────────────────────────────────

    private int getIntArgument(String name, int defaultValue) {
        for (ConfigOption<?> opt : arguments) {
            if (name.equals(opt.getName()) && opt instanceof IntegerOption i) {
                return i.getValue();
            }
        }
        return defaultValue;
    }

    private boolean getBooleanArgument(String name, boolean defaultValue) {
        for (ConfigOption<?> opt : arguments) {
            if (name.equals(opt.getName()) && opt instanceof BooleanOption b) {
                return b.getValue();
            }
        }
        return defaultValue;
    }

    @Override
    public String toString() {
        return "HighlightPathAction{active=" + active.get() + "}";
    }
}