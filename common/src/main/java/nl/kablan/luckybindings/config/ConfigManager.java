package nl.kablan.luckybindings.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nl.kablan.luckybindings.Constants;
import nl.kablan.luckybindings.action.Action;
import nl.kablan.luckybindings.keybinds.KeyBind;
import nl.kablan.luckybindings.keybinds.KeyBindManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Action.class, new ActionAdapter())
            .create();

    private static File configFile;

    public static void init(File configDir) {
        configFile = new File(configDir, "luckybindings.json");
        load();
    }

    public static void load() {
        if (configFile == null || !configFile.exists()) {
            save(); // Create default
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            if (data != null) {
                KeyBindManager.clear();
                data.dynamicKeyBinds.forEach(KeyBindManager::registerDynamic);
                data.predefinedKeyBinds.forEach(KeyBindManager::registerPredefined);
            }
        } catch (IOException e) {
            Constants.LOG.error("Failed to load config", e);
        }
    }

    public static void save() {
        if (configFile == null) return;

        ConfigData data = new ConfigData();
        data.dynamicKeyBinds = KeyBindManager.getDynamicKeyBinds();
        data.predefinedKeyBinds = KeyBindManager.getPredefinedKeyBinds();

        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            Constants.LOG.error("Failed to save config", e);
        }
    }

    private static class ConfigData {
        List<KeyBind> dynamicKeyBinds = new ArrayList<>();
        List<KeyBind> predefinedKeyBinds = new ArrayList<>();
    }
}