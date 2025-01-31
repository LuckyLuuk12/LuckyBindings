package me.luckyluuk.luckybindings.handlers;

import me.luckyluuk.luckybindings.LuckyBindings;
import me.luckyluuk.luckybindings.actions.Action;
import me.luckyluuk.luckybindings.config.ModConfig;
import me.luckyluuk.luckybindings.model.Player;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeyHandler {
  protected static final Map<String, KeyBinding> keyBindings = new HashMap<>();
  protected static ModConfig config;

  public static void initialize() {
    config = LuckyBindings.CONFIG;
    Set<String> registeredKeys = new HashSet<>();

    for (Map.Entry<String, ModConfig.KeyBind> entry : config.getKeyBinds().entrySet()) {
      String key = entry.getKey();
      ModConfig.KeyBind keyBind = entry.getValue();

      if (registeredKeys.contains(key)) {
        LuckyBindings.LOGGER.error("Duplicate key binding ID: {}", key);
        continue;
      }
      try {
        KeyBinding keyBinding = new KeyBinding(
          key,
          InputUtil.Type.KEYSYM,
          GLFW.GLFW_KEY_UNKNOWN,
          "category.luckybindings"
        );
        keyBindings.put(key, keyBinding);
        registeredKeys.add(key);
      } catch (Exception e) {
        LuckyBindings.LOGGER.error("Failed to register key binding: {}\n{}", key, e);
      }
    }
    registerKeys();
  }

  private static void registerKeys() {
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      for (Map.Entry<String, KeyBinding> entry : keyBindings.entrySet()) {
        if (entry.getValue().wasPressed()) {
          executeAction(entry.getKey());
        }
      }
    });
  }

  private static void executeAction(String key) {
    ModConfig.KeyBind keyBind = config.getKeyBinds().get(key);
    if (keyBind != null) {
      Action action = ActionFactory.createAction(keyBind.actionType(), keyBind.actionParams());
      if (action != null) {
        action.execute(Player.from(MinecraftClient.getInstance().player));
      }
    }
  }
}