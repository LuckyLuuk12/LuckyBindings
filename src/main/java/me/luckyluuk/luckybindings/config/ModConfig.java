package me.luckyluuk.luckybindings.config;


import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import lombok.Data;
import me.luckyluuk.luckybindings.model.Tuple;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Data
//@Config(name = "luckybindings")
public class ModConfig {
  public static ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
    .id(Identifier.of("luckybindings", "config"))
      .serializer(config -> GsonConfigSerializerBuilder.create(config)
        .setPath(FabricLoader.getInstance().getConfigDir().resolve("luckybindings.json5"))
        .appendGsonBuilder(GsonBuilder::setPrettyPrinting) // not needed, pretty print by default
        .setJson5(true)
        .build())
      .build();


  @SerialEntry
  public static Map<String, Tuple<String, String>> dynamicKeyBinds = new HashMap<>();
  public static Map<String, Tuple<String, String>> predefinedKeyBinds = new HashMap<>(Map.of(
    "key.luckybindings.j", new Tuple<>("prepare_chat", "/ooc "),
    "key.luckybindings.r", new Tuple<>("prepare_chat", "/f r "),
    "key.luckybindings.m", new Tuple<>("prepare_chat", "/me ")
  ));

//  /**
//   * Actually reads the JSON file to see if the key is enabled
//   * @param key The key to check
//   */
//  public static boolean isEnabled(String key) {
//    Path path = FabricLoader.getInstance().getConfigDir().resolve("luckybindings.json5");
//    File file = path.toFile();
//    if (!file.exists()) return false;
//    return
//  }

}