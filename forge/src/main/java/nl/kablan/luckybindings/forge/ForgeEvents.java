package nl.kablan.luckybindings.forge;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import nl.kablan.luckybindings.commands.LuckyBindingsCommands;

public class ForgeEvents {
    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(ForgeEvents::onRegisterCommands);
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LuckyBindingsCommands.register(event.getDispatcher());
    }
}