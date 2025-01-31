package me.luckyluuk.luckybindings.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.ConfigEntry;
import dev.isxander.yacl3.api.ConfigScreen;
import me.luckyluuk.luckybindings.LuckyBindings;
import me.luckyluuk.luckybindings.handlers.ActionFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class YaclScreenGenerator {
//  public static YetAnotherConfigLib createConfigScreen(Screen parent) {
//    YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
//      .title(Text.of("LuckyBindings Configuration"))
//      .category(createKeyBindingsCategory());
//
//    return builder.build();
//  }
//
//  private static ConfigCategory createKeyBindingsCategory() {
//    ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
//      .name(Text.of("Key Bindings"));
//
//    ModConfig config = LuckyBindings.CONFIG;
//    for (Map.Entry<String, ModConfig.KeyBind> entry : config.getKeyBinds().entrySet()) {
//      String key = entry.getKey();
//      ModConfig.KeyBind keyBind = entry.getValue();
//
//      categoryBuilder.entry(ConfigEntry.createBuilder()
//        .name(Text.of(key))
//        .description(Text.of("Action Type: " + keyBind.getActionType()))
//        .type(ConfigEntry.Type.STRING)
//        .defaultValue(keyBind.getActionType())
//        .saveConsumer(newValue -> keyBind.setActionType(newValue))
//        .build());
//    }
//
//    return categoryBuilder.build();
//  }

  public static YetAnotherConfigLib create(Screen parent) {
    return YetAnotherConfigLib.createBuilder()
      .title(Text.of("LuckyBindings Configuration"))
      .category(ConfigCategory.createBuilder()
        .name(Text.of("Key Bindings"))
        .tooltip(Text.literal("Modify, add or disable key bindings"))
        .group(OptionGroup.createBuilder()
          .name(Text.of("Predefined Key Bindings"))
          .description(OptionDescription.of(Text.of("These are the default key bindings that come with the mod")))
          // Loop over the LuckyBindings.CONFIG.getKeyBinds() map and create an Option for each key binding

          .build())
        .build())
      .build();
  }

}