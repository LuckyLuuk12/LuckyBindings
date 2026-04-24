package nl.kablan.luckybindings.forge;

import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import nl.kablan.luckybindings.commands.LuckyBindingsCommands;

@SuppressWarnings("UtilityClassWithPublicConstructor")
public class ForgeEvents {

    public ForgeEvents() {}

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        LuckyBindingsCommands.register(event.getDispatcher());
    }
}