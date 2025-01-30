package me.luckyluuk.luckybindings.handlers;

import me.luckyluuk.luckybindings.LuckyBindings;
import me.luckyluuk.luckybindings.actions.Action;
import me.luckyluuk.luckybindings.config.ModConfig;
import me.luckyluuk.luckybindings.model.Player;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class KeyHandler {
  private static final Map<String, KeyBinding> keyBindings = new HashMap<>();

  public static void initialize() {

    if(LuckyBindings.CONFIG == null) {
      LuckyBindings.CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }
    if(LuckyBindings.CONFIG.getKeyBinds() != null) {
      for(ModConfig.KeyBind keybind : LuckyBindings.CONFIG.getKeyBinds()) {
        try {
          KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            keybind.getKey(),
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_UNKNOWN,
            "category.luckybindings"
          ));
          keyBindings.put(keybind.getKey(), keyBinding);
        } catch (Exception e) {
          LuckyBindings.LOGGER.error("Failed to register key binding for key: {}\n{}", keybind.getKey(), e);
        }
      }
    }
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      for (Map.Entry<String, KeyBinding> entry : keyBindings.entrySet()) {
        if (entry.getValue().wasPressed()) {
          executeAction(entry.getKey());
        }
      }
    });
  }

  private static void executeAction(String key) {
    if(LuckyBindings.CONFIG == null) {
      LuckyBindings.CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }
    if(LuckyBindings.CONFIG.getKeyBinds() == null) return;
    for (ModConfig.KeyBind keybind : LuckyBindings.CONFIG.getKeyBinds()) {
      if (keybind.getKey().equals(key)) {
        Action action = ActionFactory.createAction(keybind.getActionType(), keybind.getActionParams());
        if (action == null) continue;
        action.execute(Player.from(MinecraftClient.getInstance().player));
      }
    }
  }
}
