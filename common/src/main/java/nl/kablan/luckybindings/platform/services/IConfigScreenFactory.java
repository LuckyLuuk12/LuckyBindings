package nl.kablan.luckybindings.platform.services;

import net.minecraft.client.gui.screens.Screen;

public interface IConfigScreenFactory {
    /**
     * Creates and returns the configuration screen for the current platform.
     * @param parent The parent screen to return to when the config screen is closed.
     * @return The configuration screen.
     */
    Screen createConfigScreen(Screen parent);
}