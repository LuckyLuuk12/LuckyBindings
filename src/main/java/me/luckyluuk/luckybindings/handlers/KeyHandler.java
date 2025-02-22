package me.luckyluuk.luckybindings.handlers;

import me.luckyluuk.luckybindings.LuckyBindings;
import me.luckyluuk.luckybindings.config.ModConfig;
import me.luckyluuk.luckybindings.model.KeyBind;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

public class KeyHandler {
  protected static final List<KeyBinding> keyBindings = new ArrayList<>();
  private static final String KEY_PREFIX = "key.luckybindings.";
  private static final String CATEGORY = "category.luckybindings";

  public static void initialize() {
    readKeys();
    register();
  }

  private static void readKeys() {
    readKeysFrom(ModConfig.dynamicKeyBinds);
    readKeysFrom(ModConfig.predefinedKeyBinds);
  }
  private static void readKeysFrom(List<KeyBind> keyBinds) {
    for (KeyBind keyBind : keyBinds) {
      if(!keyBind.isEnabled()) continue;
      String key = keyBind.getKey();
      try {
        KeyBinding keyBinding = new KeyBinding(
          KEY_PREFIX +key,
          InputUtil.Type.KEYSYM,
          getGLFWKey(key),
          CATEGORY
        );
        keyBindings.add(keyBinding);
      } catch (Exception e) {
        LuckyBindings.LOGGER.error("Failed to register key binding: {}\n{}", key, e);
      }
    }
  }

  private static int getGLFWKey(String key) {
    return InputUtil.fromTranslationKey("key.keyboard." + key).getCode();
  }

  public static void reload() {
    keyBindings.clear();
    readKeys();
    register();
    ModConfig.save();
  }

  private static void register() {
    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      for (KeyBinding keyBinding : keyBindings) {
        if (!keyBinding.wasPressed()) continue;
        executeAction(keyBinding.getTranslationKey());
      }
    });
  }

  private static void executeAction(String key) {
    KeyBind keyBind = ModConfig.dynamicKeyBinds.stream()
      .filter(kb -> (KEY_PREFIX+kb.getKey()).equals(key))
      .findFirst()
      .orElse(null);
    if (keyBind == null) { // Prefer dynamic key binds over predefined key binds
      keyBind = ModConfig.predefinedKeyBinds.stream()
        .filter(kb -> (KEY_PREFIX+kb.getKey()).equals(key))
        .findFirst()
        .orElse(null);
    }
    LuckyBindings.LOGGER.info("Key bind: {}", keyBind);
    if (keyBind == null) return;
    keyBind.setArgs(String.join(";", keyBind.getArgs()));
    keyBind.getActions().getAction().execute();
  }
}