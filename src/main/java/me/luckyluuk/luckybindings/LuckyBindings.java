package me.luckyluuk.luckybindings;

import dev.isxander.yacl3.config.GsonConfigInstance;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import me.luckyluuk.luckybindings.config.ModConfig;
import me.luckyluuk.luckybindings.handlers.KeyHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This mod aims to provide the following features:
 * - Actions to execute when a key is pressed.
 * - Execute commands, prepare messages and move player Actions
 * - Combine multiple actions into one key binding
 * - Save and load key bindings to and from a file, and share them with others
 * - ModMenu integration
 * - Server info GUIs with player list, server info, and more
 * - Pathfinding and navigation tools (e.g. particle path to a block to find your way in big maps)
 */
public class LuckyBindings implements ModInitializer {
  public static final String MOD_ID = "luckybindings";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
  public static ConfigClassHandler<ModConfig> CONFIG = ModConfig.HANDLER;

  @Override
  public void onInitialize() {
    CONFIG.load();
    KeyHandler.initialize();
  }
}
