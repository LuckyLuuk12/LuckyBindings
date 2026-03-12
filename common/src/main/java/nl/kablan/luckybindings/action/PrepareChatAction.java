package nl.kablan.luckybindings.action;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import nl.kablan.luckybindings.config.option.ConfigOption;

import java.util.List;

/**
 * Opens the chat with pre-filled text.
 */
public class PrepareChatAction implements Action {
    private final ActionType<PrepareChatAction> type;
    private final List<ConfigOption<?>> arguments;

    public PrepareChatAction(ActionType<PrepareChatAction> type, List<ConfigOption<?>> arguments) {
        this.type = type;
        this.arguments = arguments;
    }

    @Override
    public void execute(Minecraft client) {
        if (client.player == null) return;

        String text = getText();
        if (shouldSendImmediately()) {
            if (!text.isBlank()) {
                if (text.startsWith("/")) {
                    client.player.connection.sendCommand(text.substring(1));
                } else {
                    client.player.connection.sendChat(text);
                }
            }
            return;
        }

        // Keep as draft so the user can continue typing before sending.
        client.setScreen(new ChatScreen(text, true));
    }

    @Override
    public ActionType<?> getType() {
        return type;
    }

    @Override
    public List<ConfigOption<?>> getArguments() {
        return arguments;
    }

    public String getText() {
        return arguments.stream()
                .filter(opt -> opt.getName().equals("Text"))
                .map(opt -> (String) opt.getValue())
                .findFirst()
                .orElse("");
    }

    public boolean shouldSendImmediately() {
        return arguments.stream()
                .filter(opt -> opt.getName().equals("Send Immediately"))
                .map(opt -> (Boolean) opt.getValue())
                .findFirst()
                .orElse(false);
    }

    @Override
    public String toString() {
        return "PrepareChatAction{text='" + getText() + "', sendImmediately=" + shouldSendImmediately() + "}";
    }
}