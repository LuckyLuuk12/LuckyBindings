package me.luckyluuk.luckybindings.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.luckyluuk.luckybindings.LuckyBindings;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Config(name = LuckyBindings.MOD_ID)
public class ModConfig implements ConfigData {
  @ConfigEntry.Gui.CollapsibleObject
  private List<KeyBind> keyBinds = new ArrayList<>();

  /**
   * Initializes the default configuration with the following key-binds:
   * - "O" key with the action "/ooc "
   * - "M" key with the action "/me "
   * - "P" key with the Pathfinding action to 0, 0, 0
   */
  public ModConfig() {
    keyBinds.add(new KeyBind("key.keyboard.u", "prepare_chat", new String[]{"/ooc "}));
    keyBinds.add(new KeyBind("key.keyboard.n", "prepare_chat", new String[]{"/me "}));
    keyBinds.add(new KeyBind("key.keyboard.k", "path_highlight", new String[]{"0", "0", "0"}));
  }

  @Getter
  @Setter
  @AllArgsConstructor
  static public class KeyBind {
    private String key;
    private String actionType;
    private String[] actionParams;
  }
}
