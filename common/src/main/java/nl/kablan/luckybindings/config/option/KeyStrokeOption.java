package nl.kablan.luckybindings.config.option;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Config option representing a keyboard combo like CTRL+SHIFT+K.
 */
public class KeyStrokeOption extends StringOption {
    public KeyStrokeOption(String name, String description, String defaultValue) {
        this(name, description, defaultValue, null);
    }

    public KeyStrokeOption(String name, String description, String defaultValue, @Nullable String tooltip) {
        super(name, description, normalize(defaultValue), tooltip);
    }

    @Override
    public String getType() {
        return "keystroke";
    }

    @Override
    public void setValue(String value) {
        super.setValue(normalize(value));
    }

    public static String fromKeyPress(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return "";
        }

        String keyName = keyToken(keyCode, scanCode);
        if (isModifierKey(keyCode) || keyName.isEmpty()) {
            return "";
        }

        List<String> tokens = new ArrayList<>(5);
        if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            tokens.add("CTRL");
        }
        if ((modifiers & GLFW.GLFW_MOD_ALT) != 0) {
            tokens.add("ALT");
        }
        if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
            tokens.add("SHIFT");
        }
        if ((modifiers & GLFW.GLFW_MOD_SUPER) != 0) {
            tokens.add("SUPER");
        }
        tokens.add(keyName);
        return String.join("+", tokens);
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }

        String[] split = raw.toUpperCase(Locale.ROOT).replace('-', '+').split("\\+");
        boolean ctrl = false;
        boolean alt = false;
        boolean shift = false;
        boolean superKey = false;
        String key = "";

        for (String part : split) {
            String token = sanitizeToken(part);
            if (token.isEmpty()) {
                continue;
            }
            switch (token) {
                case "CTRL", "CONTROL" -> ctrl = true;
                case "ALT" -> alt = true;
                case "SHIFT" -> shift = true;
                case "SUPER", "META", "WIN", "CMD", "COMMAND" -> superKey = true;
                case "ESCAPE" -> key = "ESC";
                default -> key = token;
            }
        }

        List<String> out = new ArrayList<>(5);
        if (ctrl) out.add("CTRL");
        if (alt) out.add("ALT");
        if (shift) out.add("SHIFT");
        if (superKey) out.add("SUPER");
        if (!key.isEmpty()) out.add(key);
        return String.join("+", out);
    }

    private static String keyToken(int keyCode, int scanCode) {
        String raw = GLFW.glfwGetKeyName(keyCode, scanCode);
        if (raw == null || raw.isBlank()) {
            raw = switch (keyCode) {
                case GLFW.GLFW_KEY_ESCAPE -> "ESC";
                case GLFW.GLFW_KEY_ENTER -> "ENTER";
                case GLFW.GLFW_KEY_TAB -> "TAB";
                case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
                case GLFW.GLFW_KEY_INSERT -> "INSERT";
                case GLFW.GLFW_KEY_DELETE -> "DELETE";
                case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
                case GLFW.GLFW_KEY_LEFT -> "LEFT";
                case GLFW.GLFW_KEY_DOWN -> "DOWN";
                case GLFW.GLFW_KEY_UP -> "UP";
                case GLFW.GLFW_KEY_PAGE_UP -> "PAGE_UP";
                case GLFW.GLFW_KEY_PAGE_DOWN -> "PAGE_DOWN";
                case GLFW.GLFW_KEY_HOME -> "HOME";
                case GLFW.GLFW_KEY_END -> "END";
                case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS_LOCK";
                case GLFW.GLFW_KEY_SCROLL_LOCK -> "SCROLL_LOCK";
                case GLFW.GLFW_KEY_NUM_LOCK -> "NUM_LOCK";
                case GLFW.GLFW_KEY_PRINT_SCREEN -> "PRINT_SCREEN";
                case GLFW.GLFW_KEY_PAUSE -> "PAUSE";
                case GLFW.GLFW_KEY_F1 -> "F1";
                case GLFW.GLFW_KEY_F2 -> "F2";
                case GLFW.GLFW_KEY_F3 -> "F3";
                case GLFW.GLFW_KEY_F4 -> "F4";
                case GLFW.GLFW_KEY_F5 -> "F5";
                case GLFW.GLFW_KEY_F6 -> "F6";
                case GLFW.GLFW_KEY_F7 -> "F7";
                case GLFW.GLFW_KEY_F8 -> "F8";
                case GLFW.GLFW_KEY_F9 -> "F9";
                case GLFW.GLFW_KEY_F10 -> "F10";
                case GLFW.GLFW_KEY_F11 -> "F11";
                case GLFW.GLFW_KEY_F12 -> "F12";
                case GLFW.GLFW_KEY_SPACE -> "SPACE";
                default -> "KEY_" + keyCode;
            };
        }
        String token = sanitizeToken(raw);
        if ("ESCAPE".equals(token)) {
            return "ESC";
        }
        return token;
    }

    private static String sanitizeToken(String token) {
        return token == null ? "" : token.trim().toUpperCase(Locale.ROOT)
                .replace(' ', '_')
                .replace("KEYPAD_", "KP_")
                .replace("NUMPAD_", "KP_");
    }

    public static boolean isModifierKey(int keyCode) {
        return keyCode == GLFW.GLFW_KEY_LEFT_SHIFT
                || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT
                || keyCode == GLFW.GLFW_KEY_LEFT_CONTROL
                || keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL
                || keyCode == GLFW.GLFW_KEY_LEFT_ALT
                || keyCode == GLFW.GLFW_KEY_RIGHT_ALT
                || keyCode == GLFW.GLFW_KEY_LEFT_SUPER
                || keyCode == GLFW.GLFW_KEY_RIGHT_SUPER;
    }
}