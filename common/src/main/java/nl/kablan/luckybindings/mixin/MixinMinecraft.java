package nl.kablan.luckybindings.mixin;

import nl.kablan.luckybindings.Constants;
import nl.kablan.luckybindings.keybinds.KeyBindTickHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.Minecraft.class)
public class MixinMinecraft {
    
    @Inject(at = @At("TAIL"), method = "<init>")
    private void onInit(CallbackInfo info) {
        Constants.LOG.info("LuckyBindings: Initializing MixinMinecraft");
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void onTick(CallbackInfo info) {
        KeyBindTickHandler.onTick((net.minecraft.client.Minecraft) (Object) this);
    }
}