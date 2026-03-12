package nl.kablan.luckybindings.action;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import nl.kablan.luckybindings.config.option.ConfigOption;
import nl.kablan.luckybindings.config.option.BooleanOption;
import nl.kablan.luckybindings.config.option.EnumOption;
import nl.kablan.luckybindings.config.option.IntegerOption;

import java.util.List;

/**
 * An action that makes the player's head nod.
 */
public class NodHeadAction implements Action {
    private final ActionType<NodHeadAction> type;
    private final List<ConfigOption<?>> arguments;
    private boolean active;
    private boolean agreeAnimation;
    private int animationTick;
    private int animationDuration;
    private float pitchAmplitude;
    private float yawAmplitude;
    private int cycles;
    private String easing;
    private float lastAppliedPitchOffset;
    private float lastAppliedYawOffset;

    public NodHeadAction(ActionType<NodHeadAction> type, List<ConfigOption<?>> arguments) {
        this.type = type;
        this.arguments = arguments;
    }

    @Override
    public void execute(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;

        // Restart animation from the current pose for smooth retrigger behavior.
        this.active = true;
        this.agreeAnimation = isAgree();
        this.animationTick = 0;
        this.animationDuration = getInt(
            this.agreeAnimation ? "Agree Duration Ticks" : "Disagree Duration Ticks",
            this.agreeAnimation ? 26 : 42
        );
        this.pitchAmplitude = getInt("Agree Pitch Amplitude", 14);
        this.yawAmplitude = getInt("Disagree Yaw Amplitude", 30);
        this.cycles = Math.max(1, getInt(this.agreeAnimation ? "Agree Cycles" : "Disagree Cycles", 2));
        this.easing = getEnum("Easing", "sine");
        this.lastAppliedPitchOffset = 0.0F;
        this.lastAppliedYawOffset = 0.0F;
    }

    @Override
    public void tick(Minecraft client) {
        if (!this.active) {
            return;
        }

        LocalPlayer player = client.player;
        if (player == null) {
            this.active = false;
            return;
        }

        float progress = this.animationDuration <= 0 ? 1.0F : Math.min(1.0F, (float) this.animationTick / (float) this.animationDuration);
        float envelope = applyEasing(progress);

        if (this.agreeAnimation) {
            // Smooth pitch nod with configurable cycle count.
            float oscillation = (float) Math.sin(progress * Math.PI * 2.0D * this.cycles);
            float pitchOffset = oscillation * this.pitchAmplitude * envelope;
            applyPitchOffset(player, pitchOffset);
        } else {
            // Disagree shake: apply relative yaw offset to avoid fighting live look input.
            float oscillation = (float) Math.sin(progress * Math.PI * 2.0D * this.cycles);
            float yawOffset = oscillation * this.yawAmplitude * envelope;
            applyYawOffset(player, yawOffset);
        }

        this.animationTick++;
        if (this.animationTick > this.animationDuration) {
            // Remove any residual offsets cleanly without snapping to stale origins.
            applyPitchOffset(player, 0.0F);
            applyYawOffset(player, 0.0F);
            this.active = false;
        }
    }

    @Override
    public boolean isRunning() {
        return this.active;
    }

    @Override
    public ActionType<?> getType() {
        return type;
    }

    @Override
    public List<ConfigOption<?>> getArguments() {
        return arguments;
    }

    public boolean isAgree() {
        for (ConfigOption<?> opt : arguments) {
            if ("Agree".equals(opt.getName()) && opt instanceof BooleanOption bool) {
                return bool.getValue();
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "NodHeadAction{agree=" + isAgree() + "}";
    }

    private float applyEasing(float t) {
        return switch (this.easing) {
            case "linear" -> t;
            case "smoothstep" -> t * t * (3.0F - 2.0F * t);
            default -> easeInOutSine(t);
        };
    }

    private static float easeInOutSine(float t) {
        return (float) (0.5D - 0.5D * Math.cos(Math.PI * t));
    }

    private void applyPitchOffset(LocalPlayer player, float newOffset) {
        float basePitch = player.getXRot() - this.lastAppliedPitchOffset;
        player.setXRot(basePitch + newOffset);
        this.lastAppliedPitchOffset = newOffset;
    }

    private void applyYawOffset(LocalPlayer player, float newOffset) {
        float baseYaw = wrapDegrees(player.getYRot() - this.lastAppliedYawOffset);
        float nextYaw = wrapDegrees(baseYaw + newOffset);

        // Apply to body and head yaw so disagree animation remains visible and smooth.
        player.setYRot(nextYaw);
        player.setYHeadRot(nextYaw);
        this.lastAppliedYawOffset = newOffset;
    }

    private int getInt(String name, int fallback) {
        for (ConfigOption<?> option : this.arguments) {
            if (name.equals(option.getName()) && option instanceof IntegerOption integerOption) {
                return integerOption.getValue();
            }
        }
        return fallback;
    }

    private String getEnum(String name, String fallback) {
        for (ConfigOption<?> option : this.arguments) {
            if (name.equals(option.getName()) && option instanceof EnumOption enumOption) {
                return enumOption.getValue();
            }
        }
        return fallback;
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