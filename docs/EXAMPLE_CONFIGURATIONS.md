# Example Configurations

## Follow Path Action with Multiple Blocks

### Configuration JSON Example

```json
{
  "key": "follow stones",
  "actions": [
    {
      "type": "follow_path",
      "arguments": [
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
              "value": "stone_stairs"
            },
            {
              "name": "Block",
              "type": "block",
              "value": "stone_slab"
            }
          ]
        },
        {
          "name": "Sprint",
          "type": "boolean",
          "value": true
        },
        {
          "name": "Max Radius",
          "type": "integer",
          "value": 64
        },
        {
          "name": "Max Rounds",
          "type": "integer",
          "value": 2
        },
        {
          "name": "Max Gap Size",
          "type": "integer",
          "value": 1
        },
        {
          "name": "Max Nodes",
          "type": "integer",
          "value": 4096
        }
      ]
    }
  ],
  "description": "Follow stone pathway including stairs and slabs",
  "enabled": true,
  "sequential": true,
  "holdToRepeat": false,
  "repeatDelayTicks": 20,
  "triggerCondition": "ON_PRESS"
}
```

## Creating a Custom Action with ListOption

### Example: Multi-Target Jump Action

```java
import nl.kablan.luckybindings.action.Action;
import nl.kablan.luckybindings.action.ActionRegistry;
import nl.kablan.luckybindings.action.ActionType;
import nl.kablan.luckybindings.config.option.ConfigOption;
import nl.kablan.luckybindings.config.option.BlockOption;
import nl.kablan.luckybindings.config.option.ListOption;
import nl.kablan.luckybindings.config.option.IntegerOption;
import java.util.ArrayList;
import java.util.List;

public class MultiTargetJumpAction implements Action {
    private final ActionType<MultiTargetJumpAction> type;
    private final List<ConfigOption<?>> arguments;

    public MultiTargetJumpAction(ActionType<MultiTargetJumpAction> type, List<ConfigOption<?>> arguments) {
        this.type = type;
        this.arguments = arguments;
    }

    @Override
    public void execute(Minecraft client) {
        // Get the list of target blocks
        ListOption<?> targetBlocks = arguments.stream()
            .filter(opt -> opt instanceof ListOption && opt.getName().equals("Target Blocks"))
            .map(opt -> (ListOption<?>) opt)
            .findFirst()
            .orElse(null);

        if (targetBlocks == null || targetBlocks.size() == 0) {
            client.player.displayClientMessage(Component.literal("No target blocks configured!"), false);
            return;
        }

        // For each target block, find the nearest one and jump to it
        for (int i = 0; i < targetBlocks.size(); i++) {
            ConfigOption<?> item = targetBlocks.getItem(i);
            if (item instanceof BlockOption blockOpt) {
                // Jump logic here...
            }
        }
    }

    @Override
    public ActionType<?> getType() {
        return type;
    }

    @Override
    public List<ConfigOption<?>> getArguments() {
        return arguments;
    }
}

// Registration
public class ActionRegistry {
    public static final ActionType<MultiTargetJumpAction> MULTI_JUMP = register(new ActionType<>(
        "multi_jump",
        "Jump to the nearest of multiple target block types",
        List.of(
            new ListOption<>(
                "Target Blocks",
                "Block types to jump to (nearest one wins)",
                new ArrayList<>(List.of(
                    new BlockOption("Block 1", "", "oak_leaves"),
                    new BlockOption("Block 2", "", "spruce_leaves")
                )),
                ConfigOption.class
            ),
            new IntegerOption("Jump Height", "How high to jump", 10, 1, 20)
        ),
        MultiTargetJumpAction::new
    ));
}
```

## Using SetOption for Allowed Items

### Example: Multi-Block Interact Action

```java
import nl.kablan.luckybindings.config.option.SetOption;
import java.util.HashSet;
import java.util.Set;

public class InteractWithBlocksAction implements Action {
    // ...

    @Override
    public void execute(Minecraft client) {
        SetOption<?> allowedBlocks = arguments.stream()
            .filter(opt -> opt instanceof SetOption && opt.getName().equals("Allowed Blocks"))
            .map(opt -> (SetOption<?>) opt)
            .findFirst()
            .orElse(null);

        if (allowedBlocks == null || allowedBlocks.size() == 0) {
            client.player.displayClientMessage(Component.literal("No blocks allowed!"), false);
            return;
        }

        // Interact with all allowed blocks nearby
        for (ConfigOption<?> item : allowedBlocks.getItems()) {
            if (item instanceof BlockOption blockOpt) {
                Block block = blockOpt.getBlock();
                // Interaction logic here...
            }
        }
    }
}

// Registration
public static final ActionType<InteractWithBlocksAction> INTERACT_BLOCKS = register(new ActionType<>(
    "interact_blocks",
    "Interact with a set of allowed block types",
    List.of(
        new SetOption<>(
            "Allowed Blocks",
            "Block types that can be interacted with",
            new HashSet<>(List.of(
                new BlockOption("Button", "", "oak_button"),
                new BlockOption("Door", "", "oak_door"),
                new BlockOption("Lever", "", "lever")
            )),
            ConfigOption.class
        ),
        new IntegerOption("Interact Range", "How far to reach", 6, 1, 20)
    ),
    InteractWithBlocksAction::new
));
```

## Configuration File with Multiple Actions Using Lists

```json
{
  "dynamicKeyBinds": [
    {
      "key": "follow_wood",
      "actions": [
        {
          "type": "follow_path",
          "arguments": [
            {
              "name": "Blocks to Follow",
              "type": "list",
              "value": [
                {"name": "Block", "type": "block", "value": "oak_log"},
                {"name": "Block", "type": "block", "value": "spruce_log"},
                {"name": "Block", "type": "block", "value": "birch_log"},
                {"name": "Block", "type": "block", "value": "jungle_log"},
                {"name": "Block", "type": "block", "value": "acacia_log"},
                {"name": "Block", "type": "block", "value": "dark_oak_log"}
              ]
            },
            {
              "name": "Sprint",
              "type": "boolean",
              "value": true
            },
            {
              "name": "Max Radius",
              "type": "integer",
              "value": 128
            },
            {
              "name": "Max Rounds",
              "type": "integer",
              "value": 3
            },
            {
              "name": "Max Gap Size",
              "type": "integer",
              "value": 2
            },
            {
              "name": "Max Nodes",
              "type": "integer",
              "value": 8000
            }
          ]
        }
      ],
      "description": "Follow wooden logs (all types)",
      "enabled": true,
      "sequential": true,
      "holdToRepeat": false,
      "repeatDelayTicks": 20,
      "triggerCondition": "ON_PRESS"
    }
  ],
  "predefinedKeyBinds": []
}
```

## Nested ListOption Example (Advanced)

```java
// A list of lists - for complex configurations
ListOption<ConfigOption<?>> mainList = new ListOption<>(
    "Path Groups",
    "Multiple path configurations",
    new ArrayList<>(List.of(
        new ListOption<>(
            "Path 1 Blocks",
            "Blocks for first path",
            new ArrayList<>(List.of(new BlockOption("Block", "", "stone"))),
            ConfigOption.class
        )
    )),
    ConfigOption.class
);
```

## Tips and Best Practices

### Creating Default Items

When creating a ListOption, always provide at least one default item so users have something to clone:

```java
// Good - has a sensible default
new ListOption<>(
    "Items",
    "List of items",
    new ArrayList<>(List.of(new BlockOption("Example", "", "stone"))),
    ConfigOption.class
)

// Bad - empty list confuses users
new ListOption<>(
    "Items",
    "List of items",
    new ArrayList<>(),  // Empty!
    ConfigOption.class
)
```

### Descriptive Names

Use clear, descriptive names in the ListOption itself:

```java
// Good - tells user what each item represents
new ListOption<>(
    "Blocks to Follow",
    "Add all block types that should be part of the path",
    ...
)

// Bad - unclear
new ListOption<>(
    "List",
    "Items",
    ...
)
```

### Item Name Consistency

When creating default items, keep the item names consistent:

```java
// Good - consistent naming
new ArrayList<>(List.of(
    new BlockOption("Block", "", "stone"),
    new BlockOption("Block", "", "oak_log"),
    new BlockOption("Block", "", "grass_block")
))

// Not ideal - inconsistent naming
new ArrayList<>(List.of(
    new BlockOption("Primary Block", "", "stone"),
    new BlockOption("Secondary Block", "", "oak_log"),
    new BlockOption("Tertiary Block", "", "grass_block")
))
```

### Validation in Items

Perform validation in the action, not the option:

```java
// In the action's execute() method
for (int i = 0; i < blockList.size(); i++) {
    ConfigOption<?> item = blockList.getItem(i);
    if (item instanceof BlockOption blockOpt) {
        if (blockOpt.isValidBlock()) {
            // Use block
        } else {
            // Log or report error
        }
    }
}
```