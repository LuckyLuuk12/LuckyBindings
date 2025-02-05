package me.luckyluuk.luckybindings.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import me.luckyluuk.luckybindings.actions.Actions;
import me.luckyluuk.luckybindings.handlers.KeyHandler;
import me.luckyluuk.luckybindings.model.KeyBind;
import net.minecraft.text.Text;


public class YaclScreenGenerator {
  public static YetAnotherConfigLib create() {
    return YetAnotherConfigLib.createBuilder()
      .title(Text.of("LuckyBindings Configuration"))
      .save(KeyHandler::reload)
      .category(ConfigCategory.createBuilder()
        .name(Text.of("Predefined Key Bindings"))
        .tooltip(Text.literal("Enable or disable predefined key bindings"))
        .group(getPredefinedKeyBindings(OptionGroup.createBuilder())
          .build())
        .build()) // end of predefined key bindings category
      .category(getDynamicKeyBindings(ConfigCategory.createBuilder())
        .build()) // end of dynamic key bindings category
      .build();
  }


  /**
   * This method will add all the predefined key bindings to the option group
   * @param optionGroup the option group to add the key bindings to
   * @return the option group with the key bindings added
   */
  private static OptionGroup.Builder getPredefinedKeyBindings(OptionGroup.Builder optionGroup) {
    optionGroup.name(Text.of("Predefined Key Bindings"))
      .description(OptionDescription.of(Text.of("These are the default key bindings that come with the mod")));
    for (KeyBind keyBind : ModConfig.predefinedKeyBinds) {
      optionGroup.option(Option.<Boolean>createBuilder()
        .name(Text.of(keyBind.getKey() + " | " + String.join(" ", keyBind.getArgs())))
        .description(OptionDescription.of(Text.of(keyBind.getDescription())))
        .binding(
          true,
          keyBind::isEnabled,
          (v) -> {
            keyBind.setEnabled(v);
            KeyHandler.reload();
          })
        .controller(TickBoxControllerBuilder::create)
        .build());
    }
    return optionGroup;
  }

  /**
   * This method will make a "New Key Binding" button that will add an empty key binding to dynamicKeyBinds list
   * and list all the key bindings in the dynamicKeyBinds list using {@link KeyBind#createKeyBindGroup(KeyBind)}
   * @param cat the option group to add the key bindings to
   * @return the option group with the key bindings added
   */
  private static ConfigCategory.Builder getDynamicKeyBindings(ConfigCategory.Builder cat) {
    cat
      .name(Text.of("Dynamic Key Bindings"))
      .tooltip(Text.literal("Create, edit, and delete key bindings"));
    cat.option(ButtonOption.createBuilder()
      .name(Text.of("New Key Binding"))
      .description(OptionDescription.of(Text.of("Add a new key binding")))
      .action((yaclScreen, v) -> {
        ModConfig.dynamicKeyBinds.add(new KeyBind("", Actions.Nothing, "", true));
        KeyHandler.reload();
        yaclScreen.finishOrSave();
      })
      .build());
    for(KeyBind keyBind : ModConfig.dynamicKeyBinds) {
      cat.group(KeyBind.createKeyBindGroup(keyBind)).build();
    }
    return cat;
  }
}