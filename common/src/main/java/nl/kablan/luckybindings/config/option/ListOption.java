package nl.kablan.luckybindings.config.option;

import java.util.*;

/**
 * A config option that wraps a list of other config options.
 * Supports adding, removing, and reordering items while maintaining order.
 * The value is a list of ConfigOption items.
 */
public class ListOption<T extends ConfigOption<?>> extends BaseOption<List<T>> {

    public ListOption(String name, String description, List<T> defaultValue) {
        super(name, description, new ArrayList<>(defaultValue));
        // Initialize value as a separate mutable copy from the default
        setValue(new ArrayList<>(defaultValue));
    }

    @Override
    public String getType() {
        return "list";
    }


    /**
     * Add an item to the list.
     */
    public void addItem(T item) {
        getValue().add(item);
    }

    /**
     * Remove an item at the specified index.
     */
    public void removeItem(int index) {
        if (index >= 0 && index < getValue().size()) {
            getValue().remove(index);
        }
    }

    /**
     * Move an item from one position to another.
     */
    public void moveItem(int fromIndex, int toIndex) {
        List<T> list = getValue();
        if (fromIndex >= 0 && fromIndex < list.size() && toIndex >= 0 && toIndex < list.size()) {
            T item = list.remove(fromIndex);
            list.add(toIndex, item);
        }
    }

    /**
     * Get the item at the specified index.
     */
    public T getItem(int index) {
        if (index >= 0 && index < getValue().size()) {
            return getValue().get(index);
        }
        return null;
    }

    /**
     * Get the number of items in the list.
     */
    public int size() {
        return getValue().size();
    }

    /**
     * Get all items as an immutable list.
     */
    public List<T> getItems() {
        return Collections.unmodifiableList(getValue());
    }

    /**
     * Clear all items from the list.
     */
    public void clear() {
        getValue().clear();
    }
}