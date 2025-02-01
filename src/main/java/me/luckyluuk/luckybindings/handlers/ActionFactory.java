package me.luckyluuk.luckybindings.handlers;

import me.luckyluuk.luckybindings.actions.*;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

// TODO: This supports only Action<String> I guess so figure out how to work with custom Controllers for YACL
public class ActionFactory {
  @NotNull
  public static Action createAction(String actionType, String[] actionParams) {
    return switch(actionType) {
      case "path_highlight" -> {
        BlockPos target = new BlockPos(
          Integer.parseInt(actionParams[0]),
          Integer.parseInt(actionParams[1]),
          Integer.parseInt(actionParams[2])
        );
        yield actionParams.length > 3 ? new PathHighlight(target, Integer.parseInt(actionParams[3])) : new PathHighlight(target);
      }
      case "prepare_chat" -> new PrepareChat(actionParams[0]);
      case "execute_command" -> new ExecuteCommand(actionParams[0]);
      default -> new Nothing();
    };
  }
}
