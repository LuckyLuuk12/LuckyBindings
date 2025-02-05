package me.luckyluuk.luckybindings.actions;

import dev.isxander.yacl3.api.NameableEnum;
import lombok.Getter;
import me.luckyluuk.luckybindings.LuckyBindings;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Getter
public enum Actions implements NameableEnum {
  ExecuteCommand(new ExecuteCommand("")),
  NodHead(new NodHead("")),
  Nothing(new Nothing("")),
  PathHighlight(new PathHighlight("")),
  PrepareChat(new PrepareChat(""));

  private Action action;
  Actions(Action action) {
    this.action = action;
  }

  public void setArgs(String... args) {
    getAction().setArgs(args);
  }

  @Override
  public Text getDisplayName() {
    return Text.of(format(action.getTYPE()));
  }

  private String format(String s) {
    String r = s.toLowerCase().replace("_", " ");
    // Capitalize all words
    StringBuilder sb = new StringBuilder();
    for (String word : r.split(" ")) {
      sb.append(word.substring(0, 1).toUpperCase());
      sb.append(word.substring(1));
      sb.append(" ");
    }
    return sb.toString().trim();
  }

  @Nullable
  public static Actions from(@Nullable Action action) {
    if(action == null) return null;
    for (Actions a : values()) {
      if (a.action.equals(action)) {
        return a;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return action.toString();
  }
}
