package nl.kablan.luckybindings.action;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import nl.kablan.luckybindings.config.option.ConfigOption;

import java.text.DecimalFormat;
import java.util.List;

/**
 * An action that executes a command.
 */
public class ExecuteCommandAction implements Action {
    private final ActionType<ExecuteCommandAction> type;
    private final List<ConfigOption<?>> arguments;

    public ExecuteCommandAction(ActionType<ExecuteCommandAction> type, List<ConfigOption<?>> arguments) {
        this.type = type;
        this.arguments = arguments;
    }

    @Override
    public void execute(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) return;

        String command = getCommand();
        if (command == null || command.isEmpty()) return;

        String parsedCommand = parse(command, player);
        if (parsedCommand.startsWith("/")) {
            parsedCommand = parsedCommand.substring(1);
        }
        player.connection.sendCommand(parsedCommand);
    }

    @Override
    public ActionType<?> getType() {
        return type;
    }

    @Override
    public List<ConfigOption<?>> getArguments() {
        return arguments;
    }

    public String getCommand() {
        return arguments.stream()
                .filter(opt -> opt.getName().equals("Command"))
                .map(opt -> (String) opt.getValue())
                .findFirst()
                .orElse("");
    }

    private static String parse(String command, LocalPlayer player) {
        DecimalFormat df = new DecimalFormat("#.##");
        String result = command;

        if (result.contains("%main_hand%")) {
            result = result.replace("%main_hand%", player.getMainHandItem().getHoverName().getString());
        }
        if (result.contains("%off_hand%")) {
            result = result.replace("%off_hand%", player.getOffhandItem().getHoverName().getString());
        }
        if (result.contains("%player%")) {
            result = result.replace("%player%", player.getName().getString());
        }
        if (result.contains("%player_display_name%")) {
            result = result.replace("%player_display_name%", player.getDisplayName().getString());
        }
        if (result.contains("%x%")) {
            result = result.replace("%x%", df.format(player.getX()));
        }
        if (result.contains("%y%")) {
            result = result.replace("%y%", df.format(player.getY()));
        }
        if (result.contains("%z%")) {
            result = result.replace("%z%", df.format(player.getZ()));
        }
        if (result.contains("%yaw%")) {
            result = result.replace("%yaw%", df.format(player.getYRot()));
        }
        if (result.contains("%pitch%")) {
            result = result.replace("%pitch%", df.format(player.getXRot()));
        }
        if (result.contains("%head_yaw%")) {
            result = result.replace("%head_yaw%", df.format(player.getYHeadRot()));
        }

        if (result.contains("%target")) {
            HitResult hit = player.pick(20, 0, false);
            if (hit instanceof EntityHitResult entityHit) {
                var entity = entityHit.getEntity();
                if (result.contains("%target%")) {
                    result = result.replace("%target%", entity.getName().getString());
                }
                if (result.contains("%target_x%")) {
                    result = result.replace("%target_x%", df.format(entity.getX()));
                }
                if (result.contains("%target_y%")) {
                    result = result.replace("%target_y%", df.format(entity.getY()));
                }
                if (result.contains("%target_z%")) {
                    result = result.replace("%target_z%", df.format(entity.getZ()));
                }
                if (result.contains("%target_yaw%")) {
                    result = result.replace("%target_yaw%", df.format(entity.getYRot()));
                }
                if (result.contains("%target_pitch%")) {
                    result = result.replace("%target_pitch%", df.format(entity.getXRot()));
                }
                if (result.contains("%target_head_yaw%")) {
                    result = result.replace("%target_head_yaw%", df.format(entity.getYHeadRot()));
                }
                if (result.contains("%target_display_name%")) {
                    result = result.replace("%target_display_name%", entity.getDisplayName().getString());
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "ExecuteCommandAction{command='" + getCommand() + "'}";
    }
}