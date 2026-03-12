# ListOption and SetOption Usage Guide

## Overview

`ListOption` and `SetOption` are wrapper classes that allow you to manage collections of config options with add, remove, and reorder functionality directly in the configuration UI.

- **ListOption**: A list-based wrapper that maintains insertion order and allows reordering of items
- **SetOption**: A set-based wrapper that maintains uniqueness but doesn't preserve order

## Basic Usage

### Creating a ListOption

```java
import nl.kablan.luckybindings.config.option.ListOption;
import nl.kablan.luckybindings.config.option.BlockOption;
import java.util.ArrayList;
import java.util.List;

// Create a ListOption with default items
List<BlockOption> defaultBlocks = new ArrayList<>();
defaultBlocks.add(new BlockOption("Block 1", "", "stone"));
defaultBlocks.add(new BlockOption("Block 2", "", "oak_log"));

ListOption<ConfigOption<?>> blockList = new ListOption<>(
    "Blocks to Follow",
    "List of block types to follow",
    defaultBlocks,
    ConfigOption.class
);
```

### Creating a SetOption

```java
import nl.kablan.luckybindings.config.option.SetOption;
import java.util.HashSet;
import java.util.Set;

// Create a SetOption with default items
Set<BlockOption> defaultBlocks = new HashSet<>();
defaultBlocks.add(new BlockOption("Block 1", "", "stone"));
defaultBlocks.add(new BlockOption("Block 2", "", "oak_log"));

SetOption<ConfigOption<?>> blockSet = new SetOption<>(
    "Allowed Blocks",
    "Set of blocks allowed in this action",
    defaultBlocks,
    ConfigOption.class
);
```

## Using in ActionType

```java
public static final ActionType<MyAction> MY_ACTION = register(new ActionType<>(
    "my_action",
    "Description of the action",
    List.of(
        new ListOption<>(
            "Items",
            "List of items to process",
            new ArrayList<>(List.of(new StringOption("Item", "", "default"))),
            ConfigOption.class
        ),
        // ... other options
    ),
    MyAction::new
));
```

## Accessing Items in the Action

```java
public void execute(Minecraft client) {
    // Get the ListOption from arguments
    ListOption<?> listOpt = arguments.stream()
        .filter(opt -> opt instanceof ListOption && opt.getName().equals("Items"))
        .map(opt -> (ListOption<?>) opt)
        .findFirst()
        .orElse(null);

    if (listOpt != null) {
        // Iterate through items
        for (int i = 0; i < listOpt.size(); i++) {
            ConfigOption<?> item = listOpt.getItem(i);
            if (item instanceof BlockOption blockOpt) {
                Block block = blockOpt.getBlock();
                // Use the block
            }
        }
    }
}
```

## UI Features

When viewing a ListOption or SetOption in the KeyBindEditorScreen:

### ListOption Button
- Shows: "Edit List (N items)" button
- Clicking opens a new screen with:
  - List of all items with their names and types
  - "Add Item" button to add a new item (creates default based on first item type)
  - "Remove Selected" button to remove the highlighted item
  - "↑" and "↓" buttons for each item to reorder the list
  - "Done" button to close the editor

### SetOption Button
- Shows: "Edit Set (N items)" button
- Clicking opens a new screen with:
  - List of all items (no ordering controls)
  - "Add Item" button to add a new item
  - "Remove Selected" button to remove the highlighted item
  - "Done" button to close the editor

## Serialization/Deserialization

ListOption and SetOption are automatically serialized and deserialized by the ActionAdapter:

### JSON Format

```json
{
  "name": "Blocks to Follow",
  "type": "list",
  "value": [
    {
      "name": "Block",
      "type": "block",
      "value": "stone"
    },
    {
      "name": "Block",
      "type": "block",
      "value": "oak_log"
    }
  ]
}
```

## Example: Multi-Block Follow Path Action

The `FollowPathAction` now uses a `ListOption` to support following multiple block types:

```java
public static final ActionType<FollowPathAction> FOLLOW_PATH = register(new ActionType<>(
    "follow_path",
    "Follows a deterministic path over connected blocks of a specified type.",
    List.of(
        new ListOption<>(
            "Blocks to Follow",
            "List of block types to follow (e.g., stone, oak_log)",
            new ArrayList<>(List.of(new BlockOption("Block", "", "stone"))),
            ConfigOption.class
        ),
        // ... other options
    ),
    FollowPathAction::new
));
```

## Implementation Details

- Both ListOption and SetOption extend BaseOption<T>
- ListOption uses ArrayList internally to maintain order
- SetOption uses LinkedHashSet to maintain insertion order while ensuring uniqueness
- Custom item types can be created by extending ConfigOption
- Items are cloned properly during configuration cloning