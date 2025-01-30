package me.luckyluuk.luckybindings.handlers;

import me.luckyluuk.luckybindings.actions.Action;
import me.luckyluuk.luckybindings.actions.ExecuteCommand;
import me.luckyluuk.luckybindings.actions.PathHighlight;
import me.luckyluuk.luckybindings.actions.PrepareChat;
import net.minecraft.util.math.BlockPos;

public class ActionFactory {
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
      default -> null;
    };
  }
}
