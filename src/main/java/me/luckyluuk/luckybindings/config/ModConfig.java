package me.luckyluuk.luckybindings.config;


import com.google.gson.GsonBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import lombok.Data;
import me.luckyluuk.luckybindings.actions.Actions;
import me.luckyluuk.luckybindings.model.KeyBind;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModConfig {
  public static ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
    .id(Identifier.of("luckybindings", "LuckyBindings/config"))
      .serializer(config -> GsonConfigSerializerBuilder.create(config)
        .setPath(FabricLoader.getInstance().getConfigDir().resolve("LuckyBindings/luckybindings.json5"))
        .appendGsonBuilder(GsonBuilder::setPrettyPrinting) // not needed, pretty print by default
        .setJson5(true)
        .build())
      .build();


  @SerialEntry
  public static List<KeyBind> dynamicKeyBinds = new ArrayList<>();
  public static List<KeyBind> predefinedKeyBinds = getPredefined();

  private static List<KeyBind> getPredefined() {
    List<KeyBind> list = new ArrayList<>();
    list.add(new KeyBind("r", Actions.PrepareChat, "[SchoolRP] Prepares a friend reply command in chat", true, "/f r "));
    list.add(new KeyBind("g", Actions.PrepareChat, "[SchoolRP] Prepares a gesture command in chat", true, "/gesture "));
    list.add(new KeyBind("v", Actions.ExecuteCommand, "[SchoolRP] Lets you see your allowance", true, "allowance"));
    list.add(new KeyBind("y", Actions.PrepareChat, "[SchoolRP] Prepares a yell command in chat", true, "/yell "));
    list.add(new KeyBind("h", Actions.ExecuteCommand, "[SchoolRP] Uses /lay to lay down on the ground", true, "lay"));
    list.add(new KeyBind("b", Actions.ExecuteCommand, "[SchoolRP] Executes /carry to allow someone to get carried by you", true, "carry"));
    list.add(new KeyBind("u", Actions.ExecuteCommand, "[SchoolRP] Starts crawling on the ground", true, "crawl"));
    list.add(new KeyBind("j", Actions.PrepareChat, "[SchoolRP] Prepares a whisper command in chat", true, "/whisper "));
    list.add(new KeyBind("n", Actions.PrepareChat, "[SchoolRP] Prepares the /mec command in chat", true, "/mec "));
    list.add(new KeyBind("i", Actions.ExecuteCommand, "[SchoolRP] Performs a belly flop instantly", true, "bellyflop"));
    list.add(new KeyBind("k", Actions.ExecuteCommand, "[SchoolRP] Makes you start shivering", true, "shiver"));
    list.add(new KeyBind("m", Actions.PrepareChat, "[SchoolRP] Prepares a /me in chat for you", true, "/me "));
    list.add(new KeyBind("o", Actions.PrepareChat, "[SchoolRP] Prepares out of character chat (OOC)", true, "/ooc "));
    list.add(new KeyBind("semicolon", Actions.PrepareChat, "[SchoolRP] Prepares local out of character chat (LOOC)", true, "/looc "));
    list.add(new KeyBind("comma", Actions.ExecuteCommand, "[SchoolRP] Makes you sit down", true, "sit"));
    return list;
  }
}