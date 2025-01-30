package me.luckyluuk.luckybindings.actions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.luckyluuk.luckybindings.model.Player;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
abstract public class Action {
  private final String TYPE;

  public abstract void execute(@Nullable Player p);
}
