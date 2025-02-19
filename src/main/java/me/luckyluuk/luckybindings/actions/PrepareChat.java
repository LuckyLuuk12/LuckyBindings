package me.luckyluuk.luckybindings.actions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Attempts to open the chat for a player and prepare some text
 * for the player to send. The player can still modify the text before
 * sending it.
 */
public class PrepareChat extends Action {
  private String text;

  public PrepareChat(String... args) {
    super("prepare_chat", """
    Opens the chat for the player and prepares some text for them to send.
    You can specify the text to prepare by passing it as the first argument.
    If no argument is passed, the chat will be opened with no text.
    """);
    setArgs(args);
  }

  @Override
  public void setArgs(String... args) {
    this.text = args.length > 0 ? args[0] : "";
  }

  @Override
  public void execute() {
    ClientPlayerEntity p = MinecraftClient.getInstance().player;
    if (p == null) return;
    MinecraftClient.getInstance().setScreen(new ChatScreen(this.text));
  }

  @Override
  public String toString() {
    return "PrepareChat{" +
            "text='" + text + '\'' +
            '}';
  }
}
