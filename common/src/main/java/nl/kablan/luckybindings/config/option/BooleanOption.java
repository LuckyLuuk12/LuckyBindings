package nl.kablan.luckybindings.config.option;

public class BooleanOption extends BaseOption<Boolean> {
    public BooleanOption(String name, String description, Boolean defaultValue) {
        super(name, description, defaultValue);
    }
    @Override
    public String getType() { return "boolean"; }
}