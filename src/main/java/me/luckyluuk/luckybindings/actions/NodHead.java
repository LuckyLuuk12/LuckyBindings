package me.luckyluuk.luckybindings.actions;

import me.luckyluuk.luckybindings.model.Player;

public class NodHead extends Action {
  private boolean agree;

  public NodHead(String... agree) {
    super("nod_head", """
    Nods the player's head. Optionally, the player can agree or disagree.
    You can specify whether the player should agree or disagree by passing
    'true' or 'false' as the first argument. If no argument is passed, the
    player will agree.
    """);
    this.agree = agree.length == 0 || Boolean.parseBoolean(agree[0]);
  }

  @Override
  public void setArgs(String... args) {
    agree = args.length == 0 || Boolean.parseBoolean(args[0]);
  }

  @Override
  public void execute(Player p) {
    if (p == null) return;
    p.nodHead(agree);
  }

  @Override
  public String toString() {
    return "NodHead{" +
            "agree=" + agree +
            '}';
  }
}
