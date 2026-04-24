package nl.kablan.luckybindings.keybinds;

import nl.kablan.luckybindings.config.option.KeyStrokeOption;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

/**
 * Tracks currently pressed keys so keybinds can be matched consistently for
 * press, release, and hold-repeat handling.
 */
public final class KeyInputState {
    private static final Map<Integer, Integer> PRESSED_KEYS = new HashMap<>();

    private KeyInputState() {
    }

    public static void handleKeyEvent(int key, int scancode, int action) {
        if (key == GLFW.GLFW_KEY_UNKNOWN) {
            return;
        }

        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
            PRESSED_KEYS.put(key, scancode);
        } else if (action == GLFW.GLFW_RELEASE) {
            PRESSED_KEYS.remove(key);
        }
    }

    public static boolean isComboHeld(String expectedCombo) {
        String expected = KeyStrokeOption.normalize(expectedCombo);
        if (expected.isBlank()) {
            return false;
        }

        int modifiers = currentModifiers();
        for (Map.Entry<Integer, Integer> entry : PRESSED_KEYS.entrySet()) {
            int key = entry.getKey();
            if (KeyStrokeOption.isModifierKey(key)) {
                continue;
            }

            String current = KeyStrokeOption.fromKeyPress(key, entry.getValue(), modifiers);
            if (expected.equals(current)) {
                return true;
            }
        }

        return false;
    }

    public static void clear() {
        PRESSED_KEYS.clear();
    }

    private static int currentModifiers() {
        int modifiers = 0;
        if (isPressed(GLFW.GLFW_KEY_LEFT_CONTROL) || isPressed(GLFW.GLFW_KEY_RIGHT_CONTROL)) {
            modifiers |= GLFW.GLFW_MOD_CONTROL;
        }
        if (isPressed(GLFW.GLFW_KEY_LEFT_ALT) || isPressed(GLFW.GLFW_KEY_RIGHT_ALT)) {
            modifiers |= GLFW.GLFW_MOD_ALT;
        }
        if (isPressed(GLFW.GLFW_KEY_LEFT_SHIFT) || isPressed(GLFW.GLFW_KEY_RIGHT_SHIFT)) {
            modifiers |= GLFW.GLFW_MOD_SHIFT;
        }
        if (isPressed(GLFW.GLFW_KEY_LEFT_SUPER) || isPressed(GLFW.GLFW_KEY_RIGHT_SUPER)) {
            modifiers |= GLFW.GLFW_MOD_SUPER;
        }
        return modifiers;
    }

    private static boolean isPressed(int key) {
        return PRESSED_KEYS.containsKey(key);
    }
}