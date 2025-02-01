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
  @SerialEntry
  public static boolean FIRST_RUN = true;
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
  public static Map<String, Tuple<String, String>> predefinedKeyBinds = getPredefined();

  private static Map<String, Tuple<String, String>> getPredefined() {
    Map<String, Tuple<String, String>> map = new HashMap<>();
    map.put("key.luckybindings.r", new Tuple<>("prepare_chat", "/f r "));
    map.put("key.luckybindings.g", new Tuple<>("prepare_chat", "/gesture "));
    map.put("key.luckybindings.v", new Tuple<>("execute_command", "/allowance"));
    map.put("key.luckybindings.y", new Tuple<>("prepare_chat", "/yell "));
    map.put("key.luckybindings.h", new Tuple<>("prepare_chat", "/lay "));
    map.put("key.luckybindings.b", new Tuple<>("execute_command", "/carry"));
    map.put("key.luckybindings.u", new Tuple<>("execute_command", "/crawl"));
    map.put("key.luckybindings.j", new Tuple<>("prepare_chat", "/whisper "));
    map.put("key.luckybindings.n", new Tuple<>("prepare_chat", "/mec "));
    map.put("key.luckybindings.i", new Tuple<>("execute_command", "/bellyflop"));
    map.put("key.luckybindings.k", new Tuple<>("execute_command", "/shiver"));
    map.put("key.luckybindings.m", new Tuple<>("prepare_chat", "/me "));
    map.put("key.luckybindings.o", new Tuple<>("prepare_chat", "/ooc "));
    map.put("key.luckybindings.l", new Tuple<>("prepare_chat", "/looc "));
    map.put("key.luckybindings.,", new Tuple<>("execute_command", "/sit"));
    if(FIRST_RUN) {
      dynamicKeyBinds.putAll(map);
      FIRST_RUN = false;
    }
    return map;
  }

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