package me.luckyluuk.luckybindings.actions;

import me.luckyluuk.luckybindings.model.Player;

public class Nothing extends Action {
  public Nothing() {
    super("nothing");
  }

  @Override
  public void execute(Player p) {
    // Do nothing
  }

}
