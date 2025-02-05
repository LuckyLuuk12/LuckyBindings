package me.luckyluuk.luckybindings.actions;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.luckyluuk.luckybindings.model.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an action that can be executed by a player.
 * Please ensure the constructor of the subclass has a `String... args`
 * parameter to allow for arguments to be passed to the action which you
 * can parse in the class itself.
 */
@Data
@Getter @Setter
@RequiredArgsConstructor
abstract public class Action {
  private final String TYPE;
  private final String DESC;

  public abstract void execute(@Nullable Player p);

  public abstract void setArgs(String... args);
}
