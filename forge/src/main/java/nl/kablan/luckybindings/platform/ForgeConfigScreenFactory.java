package nl.kablan.luckybindings.platform;

import net.minecraft.client.gui.screens.Screen;
import nl.kablan.luckybindings.gui.LuckyBindingsScreen;
import nl.kablan.luckybindings.platform.services.IConfigScreenFactory;

public class ForgeConfigScreenFactory implements IConfigScreenFactory {
    @Override
    public Screen createConfigScreen(Screen parent) {
        return new LuckyBindingsScreen(parent);
    }
}