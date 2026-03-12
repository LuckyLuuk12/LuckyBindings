# Architecture Diagram - ListOption & SetOption

## Class Hierarchy

```
ConfigOption<T> (interface)
    ↑
    |
BaseOption<T> (abstract)
    ├─ StringOption
    ├─ IntegerOption
    ├─ BooleanOption
    ├─ EnumOption
    ├─ BlockOption
    ├─ ListOption<T extends ConfigOption<?>>
    └─ SetOption<T extends ConfigOption<?>>
```

## Type System

```
ListOption<ConfigOption<?>>
    ├─ Contains: List<ConfigOption<?>>
    ├─ Items can be: BlockOption, StringOption, etc.
    └─ Value type: List (ordered)

SetOption<ConfigOption<?>>
    ├─ Contains: Set<ConfigOption<?>>
    ├─ Items can be: BlockOption, StringOption, etc.
    └─ Value type: Set (unordered, unique)
```

## Data Flow

### Writing Configuration (User → File)

```
User Edit GUI
    ↓
KeyBindEditorScreen
    ↓
[Click "Edit List"]
    ↓
ListEditorScreen
    ├─ Add Item
    ├─ Remove Item
    └─ Move Item (up/down)
    ↓
ListOption.setValue()
    ↓
KeyBind.actions
    ↓
ActionAdapter.serialize()
    ├─ Serialize each ListOption
    ├─ Serialize each item in list
    └─ Create JSON array
    ↓
Config File (JSON)
```

### Reading Configuration (File → User)

```
Config File (JSON)
    ↓
ActionAdapter.deserialize()
    ├─ Read "type": "list"
    ├─ Create ListOption
    ├─ For each item in JSON array:
    │   ├─ Read item type
    │   ├─ Call deserializeItem()
    │   └─ Add to ListOption
    └─ Return ListOption
    ↓
Action.arguments
    ↓
KeyBindEditorScreen
    ↓
[Show "Edit List (N items)"]
    ↓
User can edit again
```

## Serialization Format

### JSON Structure

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

### Metadata Preserved
- Option name: Used for UI display
- Option type: Determines how to deserialize value
- Item name: Identifies item in list
- Item type: Determines item class to create
- Item value: The actual data

## Editor Screen Flow

### ListEditorScreen

```
┌─────────────────────────────────────┐
│  Edit List: Blocks to Follow        │
├─────────────────────────────────────┤
│  Items List (scrollable):           │
│  ┌───────────────────────────────┐  │
│  │ 1: Block (block)        [↑][↓] │  │
│  │ 2: Block (block)        [↑][↓] │  │
│  │ 3: Block (block)        [↑][↓] │  │
│  └───────────────────────────────┘  │
├─────────────────────────────────────┤
│ [Add Item] [Remove Selected] [Done] │
└─────────────────────────────────────┘
```

### SetEditorScreen

```
┌──────────────────────────────────┐
│  Edit Set: Allowed Blocks        │
├──────────────────────────────────┤
│  Items List (scrollable):        │
│  ┌────────────────────────────┐  │
│  │ 1: Block (block)           │  │
│  │ 2: Block (block)           │  │
│  │ 3: Block (block)           │  │
│  └────────────────────────────┘  │
├──────────────────────────────────┤
│ [Add Item] [Remove Selected] [Done]│
└──────────────────────────────────┘
```

## Component Interaction

```
                    KeyBindEditorScreen
                           |
                           | creates ConfigOption editor widgets
                           |
                ┌──────────┴──────────┐
                |                     |
        ListOption Widget       StringOption Widget
        "Edit List (N)"         EditBox
                |
                | [Click Button]
                |
          ListEditorScreen
                |
        ┌───────┼───────┐
        |       |       |
    Add Item  Remove  Move
       |        |       |
    Creates  Removes Reorders
    Item     Item     Items
       |       |       |
       └───────┴───────┤
                       |
                ListOption.setValue()
                       |
            Updates KeyBind.actions
```

## Action Execution Flow

```
User presses Key
    |
KeyBindManager.handleKey()
    |
Action.execute()
    |
[For FollowPathAction]
    ├─ Get ListOption "Blocks to Follow"
    ├─ Iterate through items
    │  ├─ Get BlockOption from list
    │  ├─ Validate block
    │  └─ Add to targetBlocks
    ├─ Start pathfinding with all blocks
    └─ Follow path
```

## Cloning Process (Recursive)

```
Original Action
    |
    | ActionAdapter.cloneOption()
    |
    ├─ [For each argument]
    │  ├─ If ListOption:
    │  │  ├─ Clone ListOption
    │  │  └─ For each item:
    │  │     └─ Recursively clone item
    │  ├─ If SetOption:
    │  │  ├─ Clone SetOption
    │  │  └─ For each item:
    │  │     └─ Recursively clone item
    │  └─ Otherwise: Clone normally
    |
Cloned Action
```

## Integration Points

```
┌─────────────────────────────────────────────────────┐
│              LuckyBindings System                   │
├─────────────────────────────────────────────────────┤
│                                                     │
│  KeyBindManager                                     │
│      ↓                                              │
│  KeyBind                                            │
│      ├─ key                                         │
│      ├─ description                                 │
│      └─ actions: List<Action>                       │
│           │                                         │
│           └─→ Action                                │
│               ├─ type: ActionType<?>                │
│               └─ arguments: List<ConfigOption<?>>   │
│                   ├─ StringOption                   │
│                   ├─ IntegerOption                  │
│                   ├─ BlockOption                    │
│                   ├─ ListOption (new!)              │
│                   └─ SetOption (new!)               │
│                       └─ items: List/Set<ConfigOpt> │
│                           ├─ BlockOption            │
│                           ├─ StringOption           │
│                           └─ IntegerOption          │
│                                                     │
│  GUI Integration:                                   │
│  ├─ KeyBindEditorScreen                            │
│  │  └─ createOptionEditor()                        │
│  │     ├─ BlockOption → EditBox                    │
│  │     ├─ ListOption → EditList Button             │
│  │     └─ SetOption → EditSet Button               │
│  ├─ ListEditorScreen (new!)                        │
│  │  └─ Edit List<ConfigOption<?>>                  │
│  └─ SetEditorScreen (new!)                         │
│     └─ Edit Set<ConfigOption<?>>                   │
│                                                     │
│  Serialization:                                     │
│  ├─ ActionAdapter.serialize()                      │
│  │  └─ Handles ListOption/SetOption                │
│  └─ ActionAdapter.deserialize()                    │
│     └─ Reconstructs ListOption/SetOption           │
│                                                     │
└─────────────────────────────────────────────────────┘
```

## Example: Follow Path Action Configuration

```
ActionType<FollowPathAction>
    |
    +─ arguments: List<ConfigOption<?>>
        |
        ├─ ListOption "Blocks to Follow"
        │   └─ value: List<ConfigOption<?>>
        │       ├─ BlockOption: "stone"
        │       ├─ BlockOption: "oak_log"
        │       └─ BlockOption: "spruce_log"
        │
        ├─ BooleanOption "Sprint": true
        ├─ IntegerOption "Max Radius": 64
        ├─ IntegerOption "Max Rounds": 2
        ├─ IntegerOption "Max Gap Size": 1
        └─ IntegerOption "Max Nodes": 4096
```

## Memory Model

```
ListOption Instance
├─ name: String
├─ description: String
├─ defaultValue: List<T>
├─ value: ArrayList<T>
│   ├─ BlockOption instance 1
│   ├─ BlockOption instance 2
│   └─ BlockOption instance 3
└─ itemType: Class<T>

SetOption Instance
├─ name: String
├─ description: String
├─ defaultValue: Set<T>
├─ value: LinkedHashSet<T>
│   ├─ BlockOption instance 1
│   ├─ BlockOption instance 2
│   └─ BlockOption instance 3
└─ itemType: Class<T>
```

## UI State Management

```
KeyBindEditorScreen
    └─ actionOptionList: ActionOptionList
           └─ entries: List<OptionEntry>
               └─ widget: AbstractWidget
                   ├─ StringOption → EditBox
                   ├─ BlockOption → EditBox
                   ├─ ListOption → Button "Edit List"
                   └─ SetOption → Button "Edit Set"

ListEditorScreen
    └─ itemList: ListItemList
       └─ entries: List<ListItemEntry>
           └─ [Item Display + ↑↓ buttons]
```

## Performance Characteristics

```
Operation              | ListOption      | SetOption
-----------------------|-----------------|-----------
Get by index           | O(1)            | N/A
Add item               | O(1)*           | O(1)
Remove item            | O(n)            | O(1)
Move item              | O(n)            | N/A
Check contains         | O(n)            | O(1)
Clone all items        | O(n)            | O(n)
Serialize              | O(n)            | O(n)
Deserialize            | O(n)            | O(n)

* Amortized - ArrayList occasionally reallocates
```

---

## Summary

The ListOption and SetOption system creates a clean separation between:
- **Data Model**: ConfigOption hierarchy with new wrapper classes
- **UI Layer**: KeyBindEditorScreen, ListEditorScreen, SetEditorScreen
- **Serialization**: ActionAdapter handles all JSON conversion
- **Action Logic**: FollowPathAction as reference implementation

This architecture makes it easy to extend, maintain, and reason about the system.