package me.luckyluuk.luckybindings.actions;

import me.luckyluuk.luckybindings.model.Player;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;

/**
 * Attempts to open the chat for a player and prepare some text
 * for the player to send. The player can still modify the text before
 * sending it.
 */
public class PrepareChat extends Action {
  private final String text;

  public PrepareChat(String text) {
    super("prepare_chat");
    this.text = text;
  }

  @Override
  public void execute(Player p) {
    if (p == null) return;
    MinecraftClient.getInstance().setScreen(new ChatScreen(text));
  }
}
