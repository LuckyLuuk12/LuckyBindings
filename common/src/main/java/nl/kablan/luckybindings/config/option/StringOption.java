package nl.kablan.luckybindings.config.option;

import org.jspecify.annotations.Nullable;

public class StringOption extends BaseOption<String> {
    @Nullable
    private final String tooltip;

    public StringOption(String name, String description, String defaultValue) {
        this(name, description, defaultValue, null);
    }

    public StringOption(String name, String description, String defaultValue, @Nullable String tooltip) {
        super(name, description, defaultValue);
        this.tooltip = tooltip;
    }

    @Override
    public String getType() { return "string"; }

    @Nullable
    public String getTooltip() {
        return tooltip;
    }
}