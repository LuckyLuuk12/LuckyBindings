package nl.kablan.luckybindings.action;

import net.minecraft.client.Minecraft;
import nl.kablan.luckybindings.config.option.ConfigOption;

import java.util.List;

/**
 * Represents an action that can be executed.
 */
public interface Action {
    /**
     * Executes the action for the current client.
     * @param client The Minecraft client instance.
     */
    void execute(Minecraft client);

    /**
     * Returns the type of this action.
     * @return The ActionType.
     */
    ActionType<?> getType();

    /**
     * Returns the arguments for this action.
     * @return The list of ConfigOption.
     */
    List<ConfigOption<?>> getArguments();

    /**
     * Returns true while this action is still actively running.
     * Default actions are one-shot and finish immediately.
     */
    default boolean isRunning() {
        return false;
    }

    /**
     * Called once every game tick by KeyBindTickHandler for all enabled keybinds.
     * Long-running actions implement this instead of using background schedulers.
     */
    default void tick(Minecraft client) {}
}