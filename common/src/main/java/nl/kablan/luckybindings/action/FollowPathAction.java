package nl.kablan.luckybindings.action;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import nl.kablan.luckybindings.config.option.*;
import nl.kablan.luckybindings.path.PathModePlanner;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Follows a computed path toward a player, location, or set of blocks.
 *
 * execute() always (re)starts: calling it while already following recomputes
 * the path and continues from the current position.  This makes hold-to-repeat
 * work correctly – every N ticks the path is refreshed (useful when following a
 * moving player).  Walking is driven by tick() so movement is perfectly smooth
 * (one update per game tick, never by a background scheduler).
 */
public class FollowPathAction implements Action {
    private static final double WAYPOINT_REACH_DIST = 0.5;
    private static final float WALK_TURN_DEGREES_PER_TICK = 8.0F;
    private static final float SPRINT_TURN_DEGREES_PER_TICK = 11.0F;

    private final ActionType<FollowPathAction> type;
    private final List<ConfigOption<?>> arguments;
    private final AtomicBoolean isActive = new AtomicBoolean(false);
    private final Queue<BlockPos> path = new LinkedList<>();
    private boolean yawInitialized;
    private float smoothedYaw;

    public FollowPathAction(ActionType<FollowPathAction> type, List<ConfigOption<?>> arguments) {
        this.type = type;
        this.arguments = arguments;
    }

    // ── execute ──────────────────────────────────────────────────────────────

    @Override
    public void execute(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;

        // Always restart: compute a fresh path from the current position.
        // Stop any running walk first so tick() doesn't keep moving while we load
        // the new path.
        isActive.set(false);
        path.clear();
        yawInitialized = false;

        PathModePlanner.Result result = PathModePlanner.computePath(client, player, arguments);
        if (!result.ok() || result.nodes().isEmpty()) {
            player.displayClientMessage(
                Component.literal(result.error() == null ? "No path found." : result.error()), false);
            return;
        }

        path.addAll(result.nodes());
        isActive.set(true);
        player.displayClientMessage(
            Component.literal("Following path with " + result.nodes().size() + " nodes."), false);
    }

    // ── tick – called every game tick ────────────────────────────────────────

    @Override
    public void tick(Minecraft client) {
        if (!isActive.get()) return;
        LocalPlayer player = client.player;
        if (player == null) {
            isActive.set(false);
            return;
        }

        if (path.isEmpty()) {
            isActive.set(false);
            player.setSprinting(false);
            yawInitialized = false;
            player.displayClientMessage(Component.literal("Finished path."), true);
            return;
        }

        BlockPos next = path.peek();
        double targetX = next.getX() + 0.5;
        double targetZ = next.getZ() + 0.5;
        double dx = targetX - player.getX();
        double dz = targetZ - player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (dist < WAYPOINT_REACH_DIST) {
            path.poll();
            return;
        }

        boolean sprint = getSprint();

        // Face toward the waypoint with smooth yaw interpolation.
        float targetYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
        if (!yawInitialized) {
            smoothedYaw = player.getYRot();
            yawInitialized = true;
        }
        float maxTurn = sprint ? SPRINT_TURN_DEGREES_PER_TICK : WALK_TURN_DEGREES_PER_TICK;
        smoothedYaw = approachYaw(smoothedYaw, targetYaw, maxTurn);
        player.setYRot(smoothedYaw);
        player.setYHeadRot(smoothedYaw);

        // Move: cap speed near the waypoint to avoid overshooting.
        // Sprint ≈ 0.26 b/t, walk ≈ 0.17 b/t (matches vanilla base speeds).
        double maxSpeed = sprint ? 0.26D : 0.17D;
        double speed = Math.min(maxSpeed, dist);
        double nx = dx / dist;
        double nz = dz / dist;

        // Preserve the Y component so gravity/jumping physics are unchanged.
        Vec3 current = player.getDeltaMovement();
        player.setDeltaMovement(nx * speed, current.y, nz * speed);
        player.setSprinting(sprint);
    }

    // ── Action contract ───────────────────────────────────────────────────────

    @Override
    public ActionType<?> getType() { return type; }

    @Override
    public List<ConfigOption<?>> getArguments() { return arguments; }

    @Override
    public boolean isRunning() { return isActive.get(); }

    // ── helpers ───────────────────────────────────────────────────────────────

    private boolean getSprint() {
        for (ConfigOption<?> opt : arguments) {
            if ("Sprint".equals(opt.getName()) && opt instanceof BooleanOption b) {
                return b.getValue();
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "FollowPathAction{sprint=" + getSprint() + ", active=" + isActive.get() + "}";
    }

    private static float approachYaw(float current, float target, float maxStep) {
        float delta = wrapDegrees(target - current);
        if (delta > maxStep) {
            delta = maxStep;
        } else if (delta < -maxStep) {
            delta = -maxStep;
        }
        return current + delta;
    }

    private static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }
}