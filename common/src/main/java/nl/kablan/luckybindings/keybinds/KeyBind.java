package nl.kablan.luckybindings.keybinds;

import nl.kablan.luckybindings.action.Action;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a key binding configuration.
 */
public class KeyBind {
    private String key;
    private final List<Action> actions = new ArrayList<>();
    private String description;
    private boolean enabled;
    
    // New settings
    private boolean sequential = true;
    private boolean holdToRepeat = false;
    private int repeatDelayTicks = 20;
    private TriggerCondition triggerCondition = TriggerCondition.ON_PRESS;

    public enum TriggerCondition {
        ON_PRESS,
        ON_RELEASE
    }

    public KeyBind(String key, String description, boolean enabled) {
        this.key = key;
        this.description = description;
        this.enabled = enabled;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Action> getActions() {
        return actions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addAction(Action action) {
        this.actions.add(action);
    }

    public boolean isSequential() { return sequential; }
    public void setSequential(boolean sequential) { this.sequential = sequential; }

    public boolean isHoldToRepeat() { return holdToRepeat; }
    public void setHoldToRepeat(boolean holdToRepeat) { this.holdToRepeat = holdToRepeat; }

    public int getRepeatDelayTicks() { return repeatDelayTicks; }
    public void setRepeatDelayTicks(int repeatDelayTicks) { this.repeatDelayTicks = repeatDelayTicks; }

    public TriggerCondition getTriggerCondition() { return triggerCondition; }
    public void setTriggerCondition(TriggerCondition triggerCondition) { this.triggerCondition = triggerCondition; }
}