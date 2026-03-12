package nl.kablan.luckybindings.config.option;

import java.util.*;

/**
 * A config option that wraps a set of other config options.
 * Similar to ListOption but maintains uniqueness and doesn't preserve order.
 * Uses LinkedHashSet to maintain insertion order while ensuring uniqueness.
 * Useful when the order of items doesn't matter.
 */
public class SetOption<T extends ConfigOption<?>> extends BaseOption<Set<T>> {

    public SetOption(String name, String description, Set<T> defaultValue) {
        super(name, description, new LinkedHashSet<>(defaultValue));
        // Initialize value as a separate mutable copy from the default
        setValue(new LinkedHashSet<>(defaultValue));
    }

    @Override
    public String getType() {
        return "set";
    }


    /**
     * Add an item to the set.
     */
    public boolean addItem(T item) {
        return getValue().add(item);
    }

    /**
     * Remove an item from the set.
     */
    public boolean removeItem(T item) {
        return getValue().remove(item);
    }

    /**
     * Check if the set contains an item.
     */
    public boolean contains(T item) {
        return getValue().contains(item);
    }

    /**
     * Get the number of items in the set.
     */
    public int size() {
        return getValue().size();
    }

    /**
     * Get all items as an immutable set.
     */
    public Set<T> getItems() {
        return Collections.unmodifiableSet(getValue());
    }

    /**
     * Clear all items from the set.
     */
    public void clear() {
        getValue().clear();
    }
}