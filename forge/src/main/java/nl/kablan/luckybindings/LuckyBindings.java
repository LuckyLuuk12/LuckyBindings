package nl.kablan.luckybindings;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import nl.kablan.luckybindings.commands.LuckyBindingsCommands;

@Mod(Constants.MOD_ID)
public class LuckyBindings {

    public LuckyBindings() {

        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.

        // Use Forge to bootstrap the Common mod.
        Constants.LOG.info("Hello Forge world!");
        CommonClass.init();
    }
}