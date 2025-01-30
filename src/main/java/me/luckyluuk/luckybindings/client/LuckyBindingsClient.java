package me.luckyluuk.luckybindings.client;

import me.luckyluuk.luckybindings.handlers.KeyHandler;
import net.fabricmc.api.ClientModInitializer;

public class LuckyBindingsClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
    KeyHandler.initialize();
  }
}
