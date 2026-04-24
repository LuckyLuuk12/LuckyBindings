package nl.kablan.luckybindings.config.option;

public abstract class BaseOption<T> implements ConfigOption<T> {
    private final String name;
    private final String description;
    private final T defaultValue;
    private T value;

    public BaseOption(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public String getName() { return name; }

    @Override
    public String getDescription() { return description; }

    @Override
    public T getValue() { return value; }

    @Override
    public void setValue(T value) { this.value = value; }

    @Override
    public T getDefaultValue() { return defaultValue; }
}