package me.luckyluuk.luckybindings.actions;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.gui.controllers.string.StringController;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.luckyluuk.luckybindings.model.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
abstract public class Action<T> {
  private final String TYPE;

  public abstract void execute(@Nullable Player p);

  // Each action should have a method that returns controller for the YACL configuration
  @NotNull
  public abstract ControllerBuilder<T> getController(Option<T> option);
}
