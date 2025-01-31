package me.luckyluuk.luckybindings.actions;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import me.luckyluuk.luckybindings.model.Player;
import org.jetbrains.annotations.NotNull;

public class Nothing extends Action<String> {
  public Nothing() {
    super("nothing");
  }

  @Override
  public void execute(Player p) {
    // Do nothing
  }

  @Override
  public @NotNull ControllerBuilder<String> getController(Option<String> option) {
    return StringControllerBuilder.create(option);
  }
}
