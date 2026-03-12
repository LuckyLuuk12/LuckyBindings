package nl.kablan.luckybindings;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import nl.kablan.luckybindings.commands.LuckyBindingsCommands;

@Mod(Constants.MOD_ID)
public class LuckyBindings {

    public LuckyBindings(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        LuckyBindingsCommands.register(event.getDispatcher());
    }
}