# Development Guide

## Project Structure

```
common/         ← All shared mod logic (write code here first)
  src/main/java/nl/kablan/luckybindings/
    action/         ← Action definitions and implementations
    config/         ← JSON serialization (ActionAdapter) & ConfigManager
    config/option/  ← ConfigOption types (BlockOption, ListOption, …)
    gui/            ← All Minecraft screens (KeyBindEditorScreen, etc.)
    keybinds/       ← KeyBind model, tick handler, manager
    mixin/          ← Mixins (keyboard/game loop hooks)
    path/           ← Path planning logic
    platform/       ← Platform-agnostic service interfaces
fabric/         ← Fabric-specific loader code & entrypoints
neoforge/       ← NeoForge-specific loader code & entrypoints
forge/          ← Forge-specific loader code & entrypoints
buildSrc/       ← Shared Gradle convention plugins
```

> **Rule:** `common` has **no access** to loader-specific APIs. Use `platform/services/` interfaces for anything loader-specific.

---

## Architecture

### Config Option Hierarchy

```
ConfigOption<T>  (interface)
  └─ BaseOption<T>  (abstract)
       ├─ BooleanOption
       ├─ IntegerOption
       ├─ StringOption
       ├─ EnumOption
       ├─ BlockOption
       ├─ KeyStrokeOption
       ├─ ListOption<T extends ConfigOption<?>>   ← ordered, allows duplicates
       └─ SetOption<T extends ConfigOption<?>>    ← unique items, LinkedHashSet
```

### Action System

```
ActionRegistry   ← registers ActionType instances with their default arguments
ActionType       ← defines name, description, default ConfigOptions, and factory
Action           ← runtime instance; holds cloned arguments & implements execute()
ActionAdapter    ← JSON serialization / deserialization of Action + ConfigOptions
```

### Data Flow

```
User edits in GUI (KeyBindEditorScreen)
  → arguments cloned into Action
  → ActionAdapter.serialize() → JSON config file
  → ActionAdapter.deserialize() ← JSON config file
  → action.execute(minecraft) called on key press (KeyBindTickHandler)
```

---

## Adding a New Action

1. **Create the Action class** in `common/.../action/`:

```java
public class MyAction extends Action {
    public MyAction(List<ConfigOption<?>> arguments) {
        super(arguments);
    }

    @Override
    public void execute(Minecraft client) {
        StringOption opt = (StringOption) arguments.stream()
            .filter(o -> o.getName().equals("My Option"))
            .findFirst().orElseThrow();
        // Do stuff
    }
}
```

2. **Register it** in `ActionRegistry.java`:

```java
public static final ActionType<MyAction> MY_ACTION = register(
    new ActionType<>(
        "my_action",
        "Does something cool",
        List.of(new StringOption("My Option", "Tooltip", "default")),
        MyAction::new
    )
);
```

Serialization is handled automatically by `ActionAdapter` for all built-in `ConfigOption` types.

---

## Adding a New ConfigOption

1. **Create the class** in `common/.../config/option/`, extending `BaseOption<T>`.

2. **Register serialization** in `ActionAdapter.java`:
   - `serialize()` — add a branch for `instanceof MyOption`
   - `deserialize()` / `deserializeItem()` — add a `"mytype"` case
   - `cloneOption()` — add a branch to clone it
   - `setOptionValue()` — add deserialization from JSON string

3. **Add UI support** in `KeyBindEditorScreen.createOptionEditor()` — add a branch returning an appropriate widget.

4. Use a short lowercase **JSON type string** (e.g. `"mytype"`).

> For `ListOption`/`SetOption` specifics see [`QUICK_REFERENCE.md`](QUICK_REFERENCE.md) and [`LIST_OPTION_USAGE.md`](LIST_OPTION_USAGE.md).