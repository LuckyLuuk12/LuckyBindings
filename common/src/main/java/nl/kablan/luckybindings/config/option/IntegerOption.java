package nl.kablan.luckybindings.config.option;

import org.jspecify.annotations.Nullable;

public class IntegerOption extends BaseOption<Integer> {
    private final int min;
    private final int max;
    @Nullable
    private final String tooltip;

    public IntegerOption(String name, String description, int defaultValue, int min, int max) {
        this(name, description, defaultValue, min, max, null);
    }

    public IntegerOption(String name, String description, int defaultValue, int min, int max, @Nullable String tooltip) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.tooltip = tooltip;
    }

    @Override
    public String getType() { return "integer"; }

    public int getMin() { return min; }
    public int getMax() { return max; }

    @Nullable
    public String getTooltip() { return tooltip; }

    @Override
    public void setValue(Integer value) {
        super.setValue(Math.max(min, Math.min(max, value)));
    }
}