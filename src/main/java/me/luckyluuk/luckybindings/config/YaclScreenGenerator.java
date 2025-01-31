package me.luckyluuk.luckybindings.config;

import dev.isxander.yacl3.api.*;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class YaclScreenGenerator {
  public static YetAnotherConfigLib create() {
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