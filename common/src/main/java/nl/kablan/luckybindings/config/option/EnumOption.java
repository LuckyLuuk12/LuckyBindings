package nl.kablan.luckybindings.config.option;

import java.util.Collections;
import java.util.List;

public class EnumOption extends BaseOption<String> {
    private final List<String> values;

    public EnumOption(String name, String description, String defaultValue, List<String> values) {
        super(name, description, defaultValue);
        this.values = List.copyOf(values);
        if (!this.values.contains(defaultValue) && !this.values.isEmpty()) {
            super.setValue(this.values.getFirst());
        }
    }

    @Override
    public String getType() {
        return "enum";
    }

    public List<String> getValues() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public void setValue(String value) {
        if (values.contains(value)) {
            super.setValue(value);
        }
    }
}