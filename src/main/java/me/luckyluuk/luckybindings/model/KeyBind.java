package me.luckyluuk.luckybindings.model;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import me.luckyluuk.luckybindings.actions.Actions;
import me.luckyluuk.luckybindings.handlers.KeyHandler;
import net.minecraft.text.Text;
import lombok.Data;

@Data
public class KeyBind {
  private String key;
  private Actions actions;
  private String[] args;
  private String description;
  private boolean enabled;

  public KeyBind(String key, Actions action, String description, boolean enabled, String... args) {
    this.key = key.startsWith("key.luckybindings.") ? key : "key.luckybindings." + key;
    this.actions = action;
    this.args = args;
    this.description = description;
    this.enabled = enabled;
    setArgs(String.join(";", args));
  }

  public void setArgs(String argString) {
    this.args = argString.split(";");
    this.actions.setArgs(this.args);
    this.actions.setArgs(this.args);
  }

  public static OptionGroup createKeyBindGroup(KeyBind keyBind) {
    return OptionGroup.createBuilder()
      .name(Text.of("Key Bind: " + keyBind.getKey()))
      .option(Option.<String>createBuilder()
        .name(Text.of("Key"))
        .description(OptionDescription.of(Text.of("The key to bind the action to")))
        .binding("", keyBind::getKey, (v) -> {
          keyBind.setKey(v);
          KeyHandler.reload();
        })
        .controller(StringControllerBuilder::create)
        .build())
      .option(Option.<Actions>createBuilder()
        .name(Text.of("Action"))
        .description(OptionDescription.of(Text.of("The action to perform when the key is pressed\n\nCurrent action explanation:\n" + keyBind.getActions().getAction().getDESC())))
        .binding(Actions.Nothing, keyBind::getActions, (v) -> {
          keyBind.setActions(v);
          KeyHandler.reload();
        })
        .controller(opt -> EnumControllerBuilder.create(opt).enumClass(Actions.class))
        .build())
      .option(Option.<String>createBuilder()
        .name(Text.of("Arguments"))
        .description(OptionDescription.of(Text.of("The arguments to pass to the action, separated by semicolons ( ; )")))
        .binding("", () -> String.join(";", keyBind.getArgs()), (v) -> {
          keyBind.setArgs(v);
          KeyHandler.reload();
        })
        .controller(StringControllerBuilder::create)
        .build())
      .option(Option.<String>createBuilder()
        .name(Text.of("Description"))
        .description(OptionDescription.of(Text.of("A description of what the key binding does, for your reference")))
        .binding("", keyBind::getDescription, (v) -> {
          keyBind.setDescription(v);
          KeyHandler.reload();
        })
        .controller(StringControllerBuilder::create)
        .build())
      .option(Option.<Boolean>createBuilder()
        .name(Text.of("Enabled"))
        .description(OptionDescription.of(Text.of("Whether the key binding is enabled")))
        .binding(false, keyBind::isEnabled, (v) -> {
          keyBind.setEnabled(v);
          KeyHandler.reload();
        })
        .controller(BooleanControllerBuilder::create)
        .build())
      .build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    KeyBind keyBind = (KeyBind) o;
    return key.equals(keyBind.key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }
}