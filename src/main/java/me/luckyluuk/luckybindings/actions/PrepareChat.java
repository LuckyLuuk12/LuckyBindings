package me.luckyluuk.luckybindings.actions;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import me.luckyluuk.luckybindings.model.Player;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.jetbrains.annotations.NotNull;

/**
 * Attempts to open the chat for a player and prepare some text
 * for the player to send. The player can still modify the text before
 * sending it.
 */
public class PrepareChat extends Action<String> {
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
  @Override
  public @NotNull ControllerBuilder<String> getController(Option<String> option) {
    return StringControllerBuilder.create(option);
  }
}
