package me.luckyluuk.luckybindings.actions;

public class Nothing extends Action {

  public Nothing(String... ignored) {
    super("nothing", "Does nothing.");
  }

  @Override
  public void execute() {
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
