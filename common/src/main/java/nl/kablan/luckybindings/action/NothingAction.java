package nl.kablan.luckybindings.action;

import net.minecraft.client.Minecraft;
import nl.kablan.luckybindings.config.option.ConfigOption;

import java.util.Collections;
import java.util.List;

/**
 * An action that does nothing.
 */
public class NothingAction implements Action {
    private final ActionType<NothingAction> type;

    public NothingAction(ActionType<NothingAction> type) {
        this.type = type;
    }

    @Override
    public void execute(Minecraft client) {
        // Do nothing
    }

    @Override
    public ActionType<?> getType() {
        return type;
    }

    @Override
    public List<ConfigOption<?>> getArguments() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "NothingAction{}";
    }
}