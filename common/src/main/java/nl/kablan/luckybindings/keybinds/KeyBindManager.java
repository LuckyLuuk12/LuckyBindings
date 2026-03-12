package nl.kablan.luckybindings.keybinds;

import net.minecraft.client.Minecraft;
import nl.kablan.luckybindings.Constants;
import nl.kablan.luckybindings.action.Action;
import nl.kablan.luckybindings.config.option.KeyStrokeOption;

import java.util.ArrayList;
import java.util.List;

public class KeyBindManager {
    private static final List<KeyBind> DYNAMIC_KEY_BINDS = new ArrayList<>();
    private static final List<KeyBind> PREDEFINED_KEY_BINDS = new ArrayList<>();

    public static void registerDynamic(KeyBind keyBind) {
        DYNAMIC_KEY_BINDS.add(keyBind);
    }

    public static boolean removeDynamic(KeyBind keyBind) {
        return DYNAMIC_KEY_BINDS.remove(keyBind);
    }

    public static boolean isDynamic(KeyBind keyBind) {
        return DYNAMIC_KEY_BINDS.contains(keyBind);
    }

    public static void registerPredefined(KeyBind keyBind) {
        PREDEFINED_KEY_BINDS.add(keyBind);
    }

    public static void clear() {
        DYNAMIC_KEY_BINDS.clear();
        PREDEFINED_KEY_BINDS.clear();
    }

    public static void onKeyTriggered(String translationKey) {
        onKeyTriggered(translationKey, KeyBind.TriggerCondition.ON_PRESS);
    }

    public static void onKeyTriggered(String translationKey, KeyBind.TriggerCondition triggerCondition) {
        String key = normalizeKey(translationKey);
        if (key.isBlank()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        for (KeyBind keyBind : findKeyBinds(key, triggerCondition)) {
            executeKeyBind(client, keyBind, key);
        }
    }

    public static void executeKeyBind(KeyBind keyBind) {
        if (keyBind == null) {
            return;
        }

        String key = normalizeKey(keyBind.getKey());
        if (key.isBlank()) {
            return;
        }

        executeKeyBind(Minecraft.getInstance(), keyBind, key);
    }

    public static boolean isKeyBindExecutionComplete(KeyBind keyBind) {
        if (keyBind == null) {
            return true;
        }
        for (Action action : keyBind.getActions()) {
            if (action.isRunning()) {
                return false;
            }
        }
        return true;
    }

    private static void executeKeyBind(Minecraft client, KeyBind keyBind, String key) {
        if (!keyBind.isEnabled()) {
            return;
        }

        if (keyBind.isSequential()) {
            executeActions(client, keyBind.getActions(), key);
        } else {
            for (Action action : keyBind.getActions()) {
                executeAction(client, action, key);
            }
        }
    }

    private static void executeActions(Minecraft client, List<Action> actions, String key) {
        for (Action action : actions) {
            executeAction(client, action, key);
        }
    }

    private static void executeAction(Minecraft client, Action action, String key) {
        try {
            action.execute(client);
        } catch (Exception e) {
            Constants.LOG.error("Failed to execute action {} for key {}", action.getType().id(), key, e);
        }
    }

    private static List<KeyBind> findKeyBinds(String key, KeyBind.TriggerCondition triggerCondition) {
        List<KeyBind> matches = new ArrayList<>();
        collectMatches(matches, DYNAMIC_KEY_BINDS, key, triggerCondition);
        collectMatches(matches, PREDEFINED_KEY_BINDS, key, triggerCondition);
        return matches;
    }

    private static void collectMatches(List<KeyBind> matches, List<KeyBind> source, String key, KeyBind.TriggerCondition triggerCondition) {
        for (KeyBind keyBind : source) {
            if (!keyBind.isEnabled()) {
                continue;
            }
            if (keyBind.getTriggerCondition() != triggerCondition) {
                continue;
            }
            if (normalizeKey(keyBind.getKey()).equals(key)) {
                matches.add(keyBind);
            }
        }
    }

    private static String normalizeKey(String rawKey) {
        String key = rawKey != null && rawKey.startsWith("key.luckybindings.")
            ? rawKey.substring("key.luckybindings.".length())
            : rawKey;
        return KeyStrokeOption.normalize(key);
    }

    public static List<KeyBind> getDynamicKeyBinds() {
        return new ArrayList<>(DYNAMIC_KEY_BINDS);
    }

    public static List<KeyBind> getPredefinedKeyBinds() {
        return new ArrayList<>(PREDEFINED_KEY_BINDS);
    }

    public static List<KeyBind> getActiveKeyBinds() {
        List<KeyBind> all = new ArrayList<>(DYNAMIC_KEY_BINDS);
        all.addAll(PREDEFINED_KEY_BINDS);
        return all;
    }
}