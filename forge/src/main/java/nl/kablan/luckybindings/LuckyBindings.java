package nl.kablan.luckybindings;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import nl.kablan.luckybindings.forge.ForgeEvents;

@Mod(Constants.MOD_ID)
public class LuckyBindings {

    public LuckyBindings() {
        Constants.LOG.info("Hello Forge world!");
        CommonClass.init();
        // Register ForgeEvents to handle Forge-specific events, such as command registration.
        MinecraftForge.EVENT_BUS.register(new ForgeEvents());
    }
}