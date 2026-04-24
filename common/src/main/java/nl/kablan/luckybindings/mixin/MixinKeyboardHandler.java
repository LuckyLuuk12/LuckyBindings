package nl.kablan.luckybindings.mixin;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;
import nl.kablan.luckybindings.keybinds.KeyBind;
import nl.kablan.luckybindings.keybinds.KeyBindManager;
import nl.kablan.luckybindings.keybinds.KeyInputState;
import nl.kablan.luckybindings.config.option.KeyStrokeOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.lwjgl.glfw.GLFW;

@Mixin(KeyboardHandler.class)
public class MixinKeyboardHandler {
    @Inject(method = "keyPress", at = @At("HEAD"))
    private void onKey(long window, int action, KeyEvent event, CallbackInfo ci) {

        String combo = KeyStrokeOption.fromKeyPress(event.key(), event.scancode(), event.modifiers());

        if (action == GLFW.GLFW_RELEASE) {
            if (!combo.isBlank()) {
                KeyBindManager.onKeyTriggered(combo, KeyBind.TriggerCondition.ON_RELEASE);
            }
            KeyInputState.handleKeyEvent(event.key(), event.scancode(), action);
            return;
        }

        KeyInputState.handleKeyEvent(event.key(), event.scancode(), action);
        if (action == GLFW.GLFW_PRESS && !combo.isBlank()) {
            KeyBindManager.onKeyTriggered(combo, KeyBind.TriggerCondition.ON_PRESS);
        }
    }
}