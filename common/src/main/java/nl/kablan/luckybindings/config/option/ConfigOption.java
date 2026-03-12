package nl.kablan.luckybindings.config.option;

public interface ConfigOption<T> {
    String getName();
    String getDescription();
    T getValue();
    void setValue(T value);
    T getDefaultValue();
    
    // For UI/Serialization
    String getType();
}