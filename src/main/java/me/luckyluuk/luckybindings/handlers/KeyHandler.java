package me.luckyluuk.luckybindings.handlers;

import me.luckyluuk.luckybindings.LuckyBindings;
import me.luckyluuk.luckybindings.actions.Action;
import me.luckyluuk.luckybindings.config.ModConfig;
import me.luckyluuk.luckybindings.model.Player;
import me.luckyluuk.luckybindings.model.Tuple;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeyHandler {
  protected static final Map<String, KeyBinding> keyBindings = new HashMap<>();

  public static void initialize() {
    readDynamicKeyBinds();
    registerKeys();
  }

  private static void readDynamicKeyBinds() {
    Set<String> registeredKeys = new HashSet<>();
    for (Map.Entry<String, Tuple<String, String>> entry :  ModConfig.dynamicKeyBinds.entrySet()) {
      String key = entry.getKey();
      if (registeredKeys.contains(key)) {
        LuckyBindings.LOGGER.error("Duplicate key binding ID: {}", key);
        continue;
      }
      LuckyBindings.LOGGER.warn("Registering key binding: {}", key);
      try {
        KeyBinding keyBinding = new KeyBinding(
          key,
          InputUtil.Type.KEYSYM,
          getGLFWKey(key.substring(key.lastIndexOf(".") + 1)),
          "category.luckybindings"
        );
        keyBindings.put(key, keyBinding);
        registeredKeys.add(key);
      } catch (Exception e) {
        LuckyBindings.LOGGER.error("Failed to register key binding: {}\n{}", key, e);
      }
    }
  }

  private static int getGLFWKey(String key) {
    return InputUtil.fromTranslationKey("key.keyboard." + key).getCode();
  }
  /**
   * Clears all key bindings and use {@link #readDynamicKeyBinds()} to re-register the active key bindings.
   */
  public static void unregisterNonDynamicKeys() {
    keyBindings.clear();
    readDynamicKeyBinds();
  }

  private static void registerKeys() {
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      for (Map.Entry<String, KeyBinding> entry : keyBindings.entrySet()) {
        if (!entry.getValue().wasPressed()) continue;
        executeAction(client, entry.getKey());
      }
    });
  }

  private static void executeAction(MinecraftClient client, String key) {
    Tuple<String, String> keyBind = ModConfig.dynamicKeyBinds.get(key);
    if (keyBind == null) return;
    Action action = ActionFactory.createAction(keyBind.fst(), keyBind.snd().split(","));
    action.execute(Player.from(client.player));
  }
}