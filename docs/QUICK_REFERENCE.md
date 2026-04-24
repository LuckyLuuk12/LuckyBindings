# Quick Reference Guide - ListOption & SetOption

## At a Glance

| Feature | ListOption | SetOption |
|---------|-----------|-----------|
| Order Preserved | ✅ Yes | ❌ No (insertion order)* |
| Duplicates Allowed | ✅ Yes | ❌ No |
| Reordering | ✅ Yes (UI) | ❌ No |
| JSON Format | Array | Array |
| Use Case | Ordered sequences | Unique items |

*SetOption uses LinkedHashSet, so insertion order is maintained but not guaranteed.

## Quick Start

### 1. Creating a ListOption

```java
import nl.kablan.luckybindings.config.option.ListOption;

ListOption<ConfigOption<?>> list = new ListOption<>(
    "Name",                          // Display name
    "Description",                   // Tooltip
    new ArrayList<>(List.of(         // Default items
        new BlockOption("Block", "", "stone")
    )),
    ConfigOption.class               // Item type
);
```

### 2. Creating a SetOption

```java
import nl.kablan.luckybindings.config.option.SetOption;

SetOption<ConfigOption<?>> set = new SetOption<>(
    "Name",                          // Display name
    "Description",                   // Tooltip
    new HashSet<>(List.of(           // Default items
        new BlockOption("Block", "", "stone")
    )),
    ConfigOption.class               // Item type
);
```

### 3. Using in ActionType

```java
public static final ActionType<MyAction> MY_ACTION = register(
    new ActionType<>(
        "my_action",
        "Description",
        List.of(
            new ListOption<>(...),   // Add here
            new IntegerOption(...)
        ),
        MyAction::new
    )
);
```

### 4. Accessing in Action

```java
@Override
public void execute(Minecraft client) {
    ListOption<?> opt = arguments.stream()
        .filter(o -> o instanceof ListOption && o.getName().equals("Name"))
        .map(o -> (ListOption<?>) o)
        .findFirst()
        .orElse(null);

    if (opt != null) {
        for (int i = 0; i < opt.size(); i++) {
            ConfigOption<?> item = opt.getItem(i);
            // Use item...
        }
    }
}
```

## API Methods

### ListOption

```java
// Adding/Removing
void addItem(T item)
void removeItem(int index)

// Navigation
T getItem(int index)
int size()
List<T> getItems()

// Reordering
void moveItem(int fromIndex, int toIndex)

// Clearing
void clear()
```

### SetOption

```java
// Adding/Removing
boolean addItem(T item)
boolean removeItem(T item)

// Query
boolean contains(T item)
int size()
Set<T> getItems()

// Clearing
void clear()
```

## UI Buttons

### In KeyBindEditorScreen
- **ListOption**: "Edit List (N items)" → Opens ListEditorScreen
- **SetOption**: "Edit Set (N items)" → Opens SetEditorScreen

### In ListEditorScreen
- "Add Item" - Creates new item based on first item's type
- "Remove Selected" - Removes highlighted item
- "↑" ↓" - Move item up/down
- "Done" - Close editor

### In SetEditorScreen
- "Add Item" - Creates new item
- "Remove Selected" - Removes highlighted item
- "Done" - Close editor

## JSON Examples

### ListOption in Config
```json
{
  "name": "Blocks",
  "type": "list",
  "value": [
    {"name": "B", "type": "block", "value": "stone"},
    {"name": "B", "type": "block", "value": "oak_log"}
  ]
}
```

### SetOption in Config
```json
{
  "name": "AllowedBlocks",
  "type": "set",
  "value": [
    {"name": "B", "type": "block", "value": "stone"}
  ]
}
```

## Common Patterns

### Pattern 1: Filter by Type
```java
ListOption<?> blocks = (ListOption<?>) arguments.stream()
    .filter(o -> o instanceof ListOption && o.getName().equals("Blocks"))
    .findFirst()
    .orElse(null);
```

### Pattern 2: Cast Items
```java
for (int i = 0; i < blocks.size(); i++) {
    ConfigOption<?> item = blocks.getItem(i);
    if (item instanceof BlockOption blockOpt) {
        Block block = blockOpt.getBlock();
        // Use block
    }
}
```

### Pattern 3: Validate All
```java
List<Block> validBlocks = new ArrayList<>();
for (int i = 0; i < blocks.size(); i++) {
    ConfigOption<?> item = blocks.getItem(i);
    if (item instanceof BlockOption blockOpt && blockOpt.isValidBlock()) {
        validBlocks.add(blockOpt.getBlock());
    }
}
```

## Troubleshooting

### Issue: ListOption shows "Edit List (0 items)"
**Solution**: Add at least one default item when creating the ListOption

### Issue: Items aren't saved
**Solution**: Make sure ActionAdapter is handling your custom ConfigOption type in setOptionValue()

### Issue: New items have wrong default values
**Solution**: The UI infers type from the first item. Make sure your first default item is representative

### Issue: Type casting errors
**Solution**: Always use `@SuppressWarnings("unchecked")` when dealing with generics

### Issue: Serialization fails
**Solution**: Check that all ConfigOption subclasses have proper constructors with (name, description, defaultValue) at minimum

## File Locations

### Core Classes
- `common/src/main/java/.../config/option/ListOption.java`
- `common/src/main/java/.../config/option/SetOption.java`

### UI Classes
- `common/src/main/java/.../gui/ListEditorScreen.java`
- `common/src/main/java/.../gui/SetEditorScreen.java`

### Integration
- `common/src/main/java/.../config/ActionAdapter.java`
- `common/src/main/java/.../gui/KeyBindEditorScreen.java`

## Performance Notes

- ListOption uses ArrayList (O(1) access, O(n) insertion/deletion)
- SetOption uses LinkedHashSet (O(1) access, O(1) add/remove)
- Cloning is O(n) where n is number of items
- Serialization is O(n) where n is number of items
- UI rendering is O(n) for ListItemList

For most use cases, lists under 100 items perform fine.

## Type Support

Automatically supported in ListOption/SetOption:
- ✅ BlockOption
- ✅ StringOption
- ✅ IntegerOption
- ✅ BooleanOption
- ✅ EnumOption
- ✅ Custom ConfigOption subclasses

## Best Practices

1. **Always provide defaults**: At least one item in the default list
2. **Consistent naming**: Name items consistently (e.g., "Block", "Block", "Block")
3. **Validate items**: Check validity in the action, not the option
4. **Handle empty lists**: Always check `size() > 0` before accessing
5. **Use descriptive names**: Clear about what each item represents
6. **Document the list**: Use description to explain what items should contain

## Examples

### Follow Multiple Block Types
```java
new ListOption<>(
    "Blocks to Follow",
    "Add all block types that should be part of the path",
    new ArrayList<>(List.of(new BlockOption("Block", "", "stone"))),
    ConfigOption.class
)
```

### Allowed Interactions
```java
new SetOption<>(
    "Allowed Blocks",
    "Block types that can be interacted with",
    new HashSet<>(List.of(
        new BlockOption("Block", "", "oak_button"),
        new BlockOption("Block", "", "lever")
    )),
    ConfigOption.class
)
```

---

For more details, see:
- `LIST_OPTION_USAGE.md` - Full API reference
- `EXAMPLE_CONFIGURATIONS.md` - Complete examples
- `FEATURE_SUMMARY.md` - Implementation overview