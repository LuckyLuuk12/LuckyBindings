package me.luckyluuk.luckybindings.config;

import lombok.Data;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Data
@Config(name = "luckybindings")
public class ModConfig implements ConfigData {
  @ConfigEntry.Gui.Excluded
  private Map<String, KeyBind> keyBinds = new HashMap<>();

  public record KeyBind(@NotNull String actionType, @NotNull String[] actionParams) {
    public KeyBind(String actionType, String[] actionParams) {
      this.actionType = actionType == null ? "" : actionType;
      this.actionParams = actionParams == null ? new String[0] : actionParams;
    }
  }


}