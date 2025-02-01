package me.luckyluuk.luckybindings.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import me.luckyluuk.luckybindings.handlers.KeyHandler;
import me.luckyluuk.luckybindings.model.Tuple;
import net.minecraft.text.Text;

import java.util.Map;

public class YaclScreenGenerator {
  public static YetAnotherConfigLib create() {
    return YetAnotherConfigLib.createBuilder()
      .title(Text.of("LuckyBindings Configuration"))
//      .save(ModConfig::save)
      .category(ConfigCategory.createBuilder()
        .name(Text.of("Key Bindings"))
        .tooltip(Text.literal("Modify, add or disable key bindings"))
        .group(getPredefinedKeyBindings(OptionGroup.createBuilder())
//          .name(Text.of("Predefined Key Bindings"))
//          .description(OptionDescription.of(Text.of("These are the default key bindings that come with the mod")))
//          // Loop over the LuckyBindings.CONFIG.getKeyBinds() map and create an Option for each key binding
//          .option(Option.<Boolean>createBuilder()
//            .name(Text.of("OOC Chat"))
//            .description(OptionDescription.of(Text.of("Opens the chat with /ooc")))
//            .binding(
//              true,
//              () -> ModConfig.keyBinds.containsKey("key.keyboard.j"),
//              (v) -> {
//                if (v) ModConfig.keyBinds.put("key.keyboard.j", new Tuple<>("prepare_chat", "/ooc "));
//                else ModConfig.keyBinds.remove("key.keyboard.j");
//              })
//            .controller(TickBoxControllerBuilder::create)
//            .build())
            .build())
          .build())
        .build();
  }


  private static OptionGroup.Builder  getPredefinedKeyBindings(OptionGroup.Builder optionGroup) {
    for(Map.Entry<String, Tuple<String, String>> entry : ModConfig.predefinedKeyBinds.entrySet()) {
      optionGroup.option(Option.<Boolean>createBuilder()
        .name(Text.of(entry.getKey().substring(entry.getKey().lastIndexOf(".")+1) + entry.getValue().fst() + entry.getValue().snd()))
        .description(OptionDescription.of(Text.of("")))
        .binding(
          true,
          () -> ModConfig.dynamicKeyBinds.containsKey(entry.getKey()),
          (v) -> {
            if (v) ModConfig.dynamicKeyBinds.put(entry.getKey(), entry.getValue());
            else ModConfig.dynamicKeyBinds.remove(entry.getKey());
            KeyHandler.unregisterNonDynamicKeys();
          })
        .controller(TickBoxControllerBuilder::create)
        .build());
    }
    return optionGroup;
  }

}