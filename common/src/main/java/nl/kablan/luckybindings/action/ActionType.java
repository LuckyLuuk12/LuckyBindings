package nl.kablan.luckybindings.action;

import nl.kablan.luckybindings.config.option.ConfigOption;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Represents a type of action.
 * @param <T> The type of Action subclass.
 */
public record ActionType<T extends Action>(
        String id,
        String description,
        List<ConfigOption<?>> argumentTemplates,
        BiFunction<ActionType<T>, List<ConfigOption<?>>, T> factory
) {
    public T create(List<ConfigOption<?>> args) {
        return factory.apply(this, args);
    }
}