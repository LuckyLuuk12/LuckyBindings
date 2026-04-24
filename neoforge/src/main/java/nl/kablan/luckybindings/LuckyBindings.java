package nl.kablan.luckybindings;


import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;

import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import nl.kablan.luckybindings.commands.LuckyBindingsCommands;
import nl.kablan.luckybindings.platform.Services;

@Mod(Constants.MOD_ID)
public class LuckyBindings {

    public LuckyBindings(IEventBus eventBus) {

        // This method is invoked by the NeoForge mod loader when it is ready
        // to load your mod. You can access NeoForge and Common code in this
        // project.

        // Use NeoForge to bootstrap the Common mod.
        Constants.LOG.info("Hello NeoForge world!");
        CommonClass.init();

        // NeoForge does not have ModMenu; expose config via client command.
        NeoForge.EVENT_BUS.addListener(this::onRegisterClientCommands);

        // It has a kinda built-in ModMenu tho for which we can just register our config screen:
        ModLoadingContext.get().registerExtensionPoint(
          IConfigScreenFactory.class,
          () -> (minecraft, parent) -> Services.CONFIG_SCREEN.createConfigScreen(parent)
        );
    }

    private void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        LuckyBindingsCommands.register(event.getDispatcher());
    }
}