package me.luckyluuk.luckybindings.actions;

import me.luckyluuk.luckybindings.model.Player;

public class Nothing extends Action {

  public Nothing(String... ignored) {
    super("nothing", "Does nothing.");
  }

  @Override
  public void execute(Player p) {
    // Do nothing
  }

  @Override
  public void setArgs(String... args) {
    // Do nothing
  }

  @Override
  public String toString() {
    return "Nothing{}";
  }

}
