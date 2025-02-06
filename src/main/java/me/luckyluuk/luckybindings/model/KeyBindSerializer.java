package me.luckyluuk.luckybindings.model;

import com.google.gson.*;
import me.luckyluuk.luckybindings.actions.Actions;

import java.lang.reflect.Type;

public class KeyBindSerializer implements JsonSerializer<KeyBind>, JsonDeserializer<KeyBind> {
  @Override
  public JsonElement serialize(KeyBind src, Type typeOfSrc, JsonSerializationContext context) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("key", src.getKey());
    jsonObject.addProperty("action", src.getActions().name());
    jsonObject.addProperty("description", src.getDescription());
    jsonObject.addProperty("enabled", src.isEnabled());
    jsonObject.add("args", context.serialize(src.getArgs()));
    return jsonObject;
  }

  @Override
  public KeyBind deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonObject jsonObject = json.getAsJsonObject();
    String key = jsonObject.get("key").getAsString();
    String action = jsonObject.get("action").getAsString();
    String description = jsonObject.get("description").getAsString();
    boolean enabled = jsonObject.get("enabled").getAsBoolean();
    String[] args = context.deserialize(jsonObject.get("args"), String[].class);
    return new KeyBind(key, Actions.valueOf(action), description, enabled, args);
  }
}