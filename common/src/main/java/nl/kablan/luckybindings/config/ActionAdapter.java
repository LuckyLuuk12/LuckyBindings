package nl.kablan.luckybindings.config;

import com.google.gson.*;
import nl.kablan.luckybindings.action.Action;
import nl.kablan.luckybindings.action.ActionRegistry;
import nl.kablan.luckybindings.action.ActionType;
import nl.kablan.luckybindings.config.option.ConfigOption;
import nl.kablan.luckybindings.config.option.BlockOption;
import nl.kablan.luckybindings.config.option.KeyStrokeOption;
import nl.kablan.luckybindings.config.option.ListOption;
import nl.kablan.luckybindings.config.option.SetOption;
import nl.kablan.luckybindings.path.PathModePlanner;

import java.lang.reflect.Type;
import java.util.*;

public class ActionAdapter implements JsonSerializer<Action>, JsonDeserializer<Action> {
    @Override
    public JsonElement serialize(Action action, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("type", action.getType().id());
        JsonArray argsArray = new JsonArray();
        for (ConfigOption<?> option : action.getArguments()) {
            JsonObject optJson = new JsonObject();
            optJson.addProperty("name", option.getName());
            optJson.addProperty("type", option.getType());

            if (option instanceof ListOption<?> listOpt) {
                optJson.add("value", serializeItems(listOpt.getItems(), context));
            } else if (option instanceof SetOption<?> setOpt) {
                optJson.add("value", serializeItems(setOpt.getItems(), context));
            } else {
                optJson.add("value", context.serialize(option.getValue()));
            }
            argsArray.add(optJson);
        }
        json.add("arguments", argsArray);
        return json;
    }

    private JsonArray serializeItems(Iterable<? extends ConfigOption<?>> items, JsonSerializationContext context) {
        JsonArray arr = new JsonArray();
        for (ConfigOption<?> item : items) {
            JsonObject itemObj = new JsonObject();
            itemObj.addProperty("name", item.getName());
            itemObj.addProperty("type", item.getType());
            itemObj.add("value", context.serialize(item.getValue()));
            arr.add(itemObj);
        }
        return arr;
    }

    @Override
    public Action deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        String typeId = obj.get("type").getAsString();
        ActionType<?> actionType = ActionRegistry.get(typeId);
        if (actionType == null) {
            throw new JsonParseException("Unknown action type: " + typeId);
        }

        List<ConfigOption<?>> arguments = new ArrayList<>();
        for (ConfigOption<?> template : actionType.argumentTemplates()) {
            arguments.add(cloneOption(template));
        }

        if (obj.has("arguments")) {
            List<String> legacyFollowPathBlocks = new ArrayList<>();
            boolean hasSerializedPathMode = false;
            boolean hasSerializedLocationTarget = false;
            JsonArray argsArray = obj.getAsJsonArray("arguments");
            for (JsonElement argElem : argsArray) {
                JsonObject argObj = argElem.getAsJsonObject();
                String name = argObj.get("name").getAsString();
                JsonElement value = argObj.get("value");

                if (PathModePlanner.OPT_PATH_MODE.equals(name)) {
                    hasSerializedPathMode = true;
                }
                if (PathModePlanner.OPT_TARGET_X.equals(name)
                    || PathModePlanner.OPT_TARGET_Y.equals(name)
                    || PathModePlanner.OPT_TARGET_Z.equals(name)) {
                    hasSerializedLocationTarget = true;
                }

                ConfigOption<?> target = findOptionByName(arguments, name);
                if (target != null) {
                    setOptionValue(target, value);
                    continue;
                }

                // Backward compatibility: old follow_path configs used block options by name.
                if ("follow_path".equals(typeId) && isLegacyFollowPathBlockName(name) && value != null && value.isJsonPrimitive()) {
                    legacyFollowPathBlocks.add(value.getAsString());
                }
            }

            if (!legacyFollowPathBlocks.isEmpty()) {
                applyLegacyFollowPathBlocks(arguments, legacyFollowPathBlocks);
            }

            if (!hasSerializedPathMode) {
                applyDefaultPathMode(typeId, arguments, hasSerializedLocationTarget);
            }
        }

        return actionType.create(arguments);
    }

    private ConfigOption<?> cloneOption(ConfigOption<?> template) {
        try {
            if (template instanceof KeyStrokeOption k) {
                return new KeyStrokeOption(k.getName(), k.getDescription(), k.getDefaultValue(), k.getTooltip());
            } else if (template instanceof nl.kablan.luckybindings.config.option.StringOption s) {
                return new nl.kablan.luckybindings.config.option.StringOption(s.getName(), s.getDescription(), s.getDefaultValue(), s.getTooltip());
            } else if (template instanceof nl.kablan.luckybindings.config.option.BooleanOption b) {
                return new nl.kablan.luckybindings.config.option.BooleanOption(b.getName(), b.getDescription(), b.getDefaultValue());
            } else if (template instanceof nl.kablan.luckybindings.config.option.IntegerOption i) {
                return new nl.kablan.luckybindings.config.option.IntegerOption(i.getName(), i.getDescription(), i.getDefaultValue(), i.getMin(), i.getMax(), i.getTooltip());
            } else if (template instanceof nl.kablan.luckybindings.config.option.EnumOption e) {
                return new nl.kablan.luckybindings.config.option.EnumOption(e.getName(), e.getDescription(), e.getDefaultValue(), e.getValues());
            } else if (template instanceof BlockOption b) {
                return new BlockOption(b.getName(), b.getDescription(), b.getDefaultValue());
            } else if (template instanceof ListOption<?> l) {
                List<ConfigOption<?>> clonedItems = new ArrayList<>();
                for (ConfigOption<?> item : l.getItems()) {
                    clonedItems.add(cloneOption(item));
                }
                return new ListOption<>(l.getName(), l.getDescription(), clonedItems);
            } else if (template instanceof SetOption<?> s) {
                Set<ConfigOption<?>> clonedItems = new LinkedHashSet<>();
                for (ConfigOption<?> item : s.getItems()) {
                    clonedItems.add(cloneOption(item));
                }
                return new SetOption<>(s.getName(), s.getDescription(), clonedItems);
            }
        } catch (Exception ignored) {}
        return template; // Fallback
    }

    private void setOptionValue(ConfigOption<?> opt, JsonElement value) {
        if (opt instanceof KeyStrokeOption k) {
            k.setValue(value.getAsString());
        } else if (opt instanceof nl.kablan.luckybindings.config.option.StringOption s) {
            s.setValue(value.getAsString());
        } else if (opt instanceof nl.kablan.luckybindings.config.option.BooleanOption b) {
            b.setValue(value.getAsBoolean());
        } else if (opt instanceof nl.kablan.luckybindings.config.option.IntegerOption i) {
            i.setValue(value.getAsInt());
        } else if (opt instanceof nl.kablan.luckybindings.config.option.EnumOption e) {
            e.setValue(value.getAsString());
        } else if (opt instanceof BlockOption b) {
            b.setValue(value.getAsString());
        } else if (opt instanceof ListOption<?> listOpt && value.isJsonArray()) {
            listOpt.clear();
            for (JsonElement itemElem : value.getAsJsonArray()) {
                JsonObject itemObj = itemElem.getAsJsonObject();
                ConfigOption<?> item = deserializeItem(itemObj.get("type").getAsString(), itemObj);
                if (item != null) {
                    addToList(listOpt, item);
                }
            }
        } else if (opt instanceof ListOption<?> listOpt && value.isJsonPrimitive()) {
            // Backward compatibility: list option saved as a single primitive value.
            listOpt.clear();
            addToList(listOpt, new BlockOption("Block", "", value.getAsString()));
        } else if (opt instanceof SetOption<?> setOpt && value.isJsonArray()) {
            setOpt.clear();
            for (JsonElement itemElem : value.getAsJsonArray()) {
                JsonObject itemObj = itemElem.getAsJsonObject();
                ConfigOption<?> item = deserializeItem(itemObj.get("type").getAsString(), itemObj);
                if (item != null) {
                    addToSet(setOpt, item);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addToList(ListOption<?> list, ConfigOption<?> item) {
        ((ListOption<ConfigOption<?>>) list).addItem(item);
    }

    @SuppressWarnings("unchecked")
    private void addToSet(SetOption<?> set, ConfigOption<?> item) {
        ((SetOption<ConfigOption<?>>) set).addItem(item);
    }

    private ConfigOption<?> deserializeItem(String type, JsonObject itemObj) {
        try {
            String name = itemObj.has("name") ? itemObj.get("name").getAsString() : "Item";
            JsonElement value = itemObj.has("value") ? itemObj.get("value") : JsonNull.INSTANCE;

            return switch (type) {
                case "keystroke" -> new KeyStrokeOption(
                        name, "", value.isJsonNull() ? "" : value.getAsString(), null);
                case "string" -> new nl.kablan.luckybindings.config.option.StringOption(
                        name, "", value.isJsonNull() ? "" : value.getAsString(), null);
                case "boolean" -> new nl.kablan.luckybindings.config.option.BooleanOption(
                        name, "", !value.isJsonNull() && value.getAsBoolean());
                case "integer" -> new nl.kablan.luckybindings.config.option.IntegerOption(
                        name, "", value.isJsonNull() ? 0 : value.getAsInt(), 0, Integer.MAX_VALUE);
                case "block" -> new BlockOption(
                        name, "", value.isJsonNull() ? "stone" : value.getAsString());
                default -> null;
            };
        } catch (Exception ignored) {
            return null;
        }
    }

    private ConfigOption<?> findOptionByName(List<ConfigOption<?>> arguments, String name) {
        for (ConfigOption<?> option : arguments) {
            if (option.getName().equals(name)) {
                return option;
            }
        }
        return null;
    }

    private boolean isLegacyFollowPathBlockName(String name) {
        return "Primary Block".equals(name) || "Secondary Block".equals(name) || "Block Name".equals(name);
    }

    private void applyLegacyFollowPathBlocks(List<ConfigOption<?>> arguments, List<String> blockIds) {
        ConfigOption<?> blocksOpt = findOptionByName(arguments, "Blocks to Follow");
        if (!(blocksOpt instanceof ListOption<?> listOpt)) {
            return;
        }
        listOpt.clear();
        for (String blockId : blockIds) {
            if (blockId != null && !blockId.isBlank()) {
                addToList(listOpt, new BlockOption("Block", "", blockId));
            }
        }

        // Keep a safe fallback if every legacy value was empty.
        if (listOpt.size() == 0) {
            addToList(listOpt, new BlockOption("Block", "", "stone"));
        }
    }

    private void applyDefaultPathMode(String typeId, List<ConfigOption<?>> arguments, boolean hasSerializedLocationTarget) {
        ConfigOption<?> modeOpt = findOptionByName(arguments, PathModePlanner.OPT_PATH_MODE);
        if (!(modeOpt instanceof nl.kablan.luckybindings.config.option.EnumOption enumOption)) {
            return;
        }

        if ("highlight_path".equals(typeId) && hasSerializedLocationTarget) {
            enumOption.setValue(PathModePlanner.MODE_LOCATION);
            return;
        }

        if ("follow_path".equals(typeId)) {
            enumOption.setValue(PathModePlanner.MODE_BLOCKS);
        }
    }
}