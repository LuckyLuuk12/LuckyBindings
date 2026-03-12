package nl.kablan.luckybindings.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import nl.kablan.luckybindings.action.Action;
import nl.kablan.luckybindings.action.ActionRegistry;
import nl.kablan.luckybindings.action.ActionType;
import nl.kablan.luckybindings.config.option.BooleanOption;
import nl.kablan.luckybindings.config.option.ConfigOption;
import nl.kablan.luckybindings.config.option.EnumOption;
import nl.kablan.luckybindings.config.option.IntegerOption;
import nl.kablan.luckybindings.config.option.KeyStrokeOption;
import nl.kablan.luckybindings.config.option.StringOption;
import nl.kablan.luckybindings.config.option.BlockOption;
import nl.kablan.luckybindings.config.option.ListOption;
import nl.kablan.luckybindings.config.option.SetOption;
import nl.kablan.luckybindings.keybinds.KeyBind;
import nl.kablan.luckybindings.path.PathModePlanner;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
public class KeyBindEditorScreen extends Screen {
    private static final int FIELD_WIDTH = 260;
    private static final int ROW_HEIGHT = 20;
    private static final int OPTION_LABEL_WIDTH = 118;
    private static final int COLUMN_GAP = 8;
    private static final int OPTION_EDITOR_WIDTH = FIELD_WIDTH - OPTION_LABEL_WIDTH - 18;

    private final KeyBind keyBind;
    private final Screen parent;

    private KeyStrokeOption keyBindKeyOption;
    private EditBox description;
    private int selectedActionIndex = 0;
    private String selectedActionTypeId;
    private ActionOptionList actionOptionList;
    private boolean showNoActionsHint;
    private KeyStrokeOption activeKeyStrokeCapture;
    private Button activeKeyStrokeButton;

    public KeyBindEditorScreen(KeyBind keyBind, Screen parent) {
        super(Component.literal("Edit KeyBind: " + keyBind.getKey()));
        this.keyBind = keyBind;
        this.parent = parent;
    }

    @Override
    public void onClose() {
        clearKeyStrokeCapture();
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void init() {
        this.showNoActionsHint = false;
        this.actionOptionList = null;

        int x = this.width / 2 - FIELD_WIDTH / 2;
        int columnWidth = (FIELD_WIDTH - COLUMN_GAP) / 2;
        int rightX = x + columnWidth + COLUMN_GAP;
        int y = 34;

        this.keyBindKeyOption = new KeyStrokeOption(
            "Keybind",
            "Primary key combination for this keybind",
            this.keyBind.getKey(),
            "Click to capture this keybind. Press the desired combo, or press Esc while capturing to clear it."
        );
        Button keyCaptureButton = Button.builder(getKeyStrokeLabel(this.keyBindKeyOption, false),
                button -> toggleKeyStrokeCapture(this.keyBindKeyOption, button))
            .bounds(x, y, columnWidth, ROW_HEIGHT)
            .build();
        if (this.keyBindKeyOption.getTooltip() != null) {
            keyCaptureButton.setTooltip(Tooltip.create(Component.literal(this.keyBindKeyOption.getTooltip())));
        }
        this.addRenderableWidget(keyCaptureButton);

        this.description = new EditBox(this.font, rightX, y, columnWidth, ROW_HEIGHT, Component.literal("Description"));
        this.description.setValue(this.keyBind.getDescription() == null ? "" : this.keyBind.getDescription());
        this.addRenderableWidget(this.description);
        y += 24;

        this.addRenderableWidget(CycleButton.onOffBuilder(this.keyBind.isEnabled())
            .create(x, y, columnWidth, ROW_HEIGHT, Component.literal("Enabled"), (button, value) -> this.keyBind.setEnabled(value)));

        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Press"), Component.literal("Release"), this.keyBind.getTriggerCondition() == KeyBind.TriggerCondition.ON_PRESS)
            .withValues(true, false)
            .create(rightX, y, columnWidth, ROW_HEIGHT, Component.literal("Trigger"), (button, value) -> this.keyBind.setTriggerCondition(value ? KeyBind.TriggerCondition.ON_PRESS : KeyBind.TriggerCondition.ON_RELEASE)));
        y += 24;

        this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("Sequential"), Component.literal("Simultaneous"), this.keyBind.isSequential())
            .withValues(true, false)
            .create(x, y, columnWidth, ROW_HEIGHT, Component.literal("Execution Mode"), (button, value) -> this.keyBind.setSequential(value)));

        this.addRenderableWidget(CycleButton.onOffBuilder(this.keyBind.isHoldToRepeat())
            .create(rightX, y, columnWidth, ROW_HEIGHT, Component.literal("Hold to Repeat"), (button, value) -> this.keyBind.setHoldToRepeat(value)));
        y += 28;

        this.buildActionSection(x, y);

        this.addRenderableWidget(Button.builder(Component.literal("Done"), (button) -> {
            this.keyBind.setKey(this.keyBindKeyOption.getValue());
            this.keyBind.setDescription(this.description.getValue().trim());
            this.onClose();
        }).bounds(x, this.height - 28, FIELD_WIDTH, ROW_HEIGHT).build());
    }

    private void buildActionSection(int x, int y) {
        List<ActionType<?>> actionTypes = getActionTypes();
        if (this.selectedActionTypeId == null && !actionTypes.isEmpty()) {
            this.selectedActionTypeId = actionTypes.getFirst().id();
        }

        if (!actionTypes.isEmpty()) {
            ActionType<?> selectedType = actionTypes.stream()
                .filter(type -> type.id().equals(this.selectedActionTypeId))
                .findFirst()
                .orElse(actionTypes.getFirst());

            this.addRenderableWidget(CycleButton.builder(
                    (ActionType<?> type) -> Component.literal(type.id()),
                    selectedType
                )
                .withValues(actionTypes)
                .displayOnlyValue()
                .create(x, y, FIELD_WIDTH - 90, ROW_HEIGHT, Component.literal("Action Type"), (button, value) -> this.selectedActionTypeId = value.id()));

            this.addRenderableWidget(Button.builder(Component.literal("Add Action"), (button) -> {
                ActionType<?> toAdd = actionTypes.stream()
                    .filter(type -> type.id().equals(this.selectedActionTypeId))
                    .findFirst()
                    .orElse(actionTypes.getFirst());

                List<ConfigOption<?>> args = cloneOptions(toAdd.argumentTemplates());
                this.keyBind.addAction(toAdd.create(args));
                this.selectedActionIndex = this.keyBind.getActions().size() - 1;
                this.rebuildWidgets();
            }).bounds(x + FIELD_WIDTH - 85, y, 85, ROW_HEIGHT).build());
        }

        y += 24;
        List<Action> actions = this.keyBind.getActions();
        if (actions.isEmpty()) {
            this.showNoActionsHint = true;
            this.createOptionList(x, y + 12);
            return;
        }

        this.selectedActionIndex = Math.max(0, Math.min(this.selectedActionIndex, actions.size() - 1));
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < actions.size(); i++) {
            indices.add(i);
        }

        this.addRenderableWidget(CycleButton.builder(
                (Integer index) -> Component.literal(formatActionLabel(index)),
                this.selectedActionIndex
            )
            .withValues(indices)
            .displayOnlyValue()
            .create(x, y, FIELD_WIDTH - 70, ROW_HEIGHT, Component.literal("Selected Action"), (button, value) -> {
                this.selectedActionIndex = value;
                this.rebuildWidgets();
            }));

        this.addRenderableWidget(Button.builder(Component.literal("Remove"), (button) -> {
            if (!this.keyBind.getActions().isEmpty()) {
                this.keyBind.getActions().remove(this.selectedActionIndex);
                this.selectedActionIndex = Math.max(0, this.selectedActionIndex - 1);
                this.rebuildWidgets();
            }
        }).bounds(x + FIELD_WIDTH - 65, y, 65, ROW_HEIGHT).build());
        y += 24;

        this.createOptionList(x, y);
        this.refreshOptionList();
    }

    private void createOptionList(int x, int y) {
        int listHeight = Math.max(40, this.height - y - 36);
        this.actionOptionList = new ActionOptionList(this.minecraft, FIELD_WIDTH, listHeight, y, ROW_HEIGHT);
        this.actionOptionList.setX(x);
        this.addRenderableWidget(this.actionOptionList);
    }

    private void refreshOptionList() {
        if (this.actionOptionList == null) {
            return;
        }

        this.actionOptionList.clearAllEntries();
        if (this.keyBind.getActions().isEmpty()) {
            return;
        }

        Action selectedAction = this.keyBind.getActions().get(this.selectedActionIndex);
        for (ConfigOption<?> option : selectedAction.getArguments()) {
            if (shouldShowOption(selectedAction, option)) {
                this.actionOptionList.addOption(option);
            }
        }
    }

    private boolean shouldShowOption(Action action, ConfigOption<?> option) {
        String actionId = action.getType().id();
        if (!"follow_path".equals(actionId) && !"highlight_path".equals(actionId)) {
            return true;
        }

        String optionName = option.getName();
        if (PathModePlanner.OPT_PATH_MODE.equals(optionName)
            || "Range".equals(optionName)
            || "Update Interval Ticks".equals(optionName)
            || "Recompute Distance".equals(optionName)
            || "Max Render Nodes".equals(optionName)
            || "Stop When Reached".equals(optionName)
            || "Toggle Mode".equals(optionName)
            || "Sprint".equals(optionName)) {
            return true;
        }

        String mode = PathModePlanner.getPathMode(action.getArguments());
        if (PathModePlanner.MODE_PLAYER.equals(mode)) {
            return PathModePlanner.OPT_TARGET_PLAYER.equals(optionName);
        }

        if (PathModePlanner.MODE_LOCATION.equals(mode)) {
            return PathModePlanner.OPT_TARGET_X.equals(optionName)
                || PathModePlanner.OPT_TARGET_Y.equals(optionName)
                || PathModePlanner.OPT_TARGET_Z.equals(optionName);
        }

        Set<String> blockModeOptions = new HashSet<>(Set.of(
            PathModePlanner.OPT_BLOCKS_TO_FOLLOW,
            PathModePlanner.OPT_USE_VANILLA_VALIDATION,
            "Max Radius",
            "Max Rounds",
            "Max Gap Size",
            "Max Nodes"
        ));
        return blockModeOptions.contains(optionName);
    }

    private AbstractWidget createOptionEditor(ConfigOption<?> option) {
        if (option instanceof KeyStrokeOption keyStrokeOption) {
            Button button = Button.builder(getKeyStrokeLabel(keyStrokeOption, false),
                    btn -> toggleKeyStrokeCapture(keyStrokeOption, btn))
                .bounds(0, 0, OPTION_EDITOR_WIDTH, ROW_HEIGHT)
                .build();
            if (keyStrokeOption.getTooltip() != null) {
                button.setTooltip(Tooltip.create(Component.literal(keyStrokeOption.getTooltip())));
            }
            return button;
        }

        if (option instanceof StringOption stringOption) {
            EditBox box = new EditBox(this.font, 0, 0, OPTION_EDITOR_WIDTH, ROW_HEIGHT, Component.literal(stringOption.getName()));
            box.setValue(stringOption.getValue());
            box.setResponder(stringOption::setValue);
            if (stringOption.getTooltip() != null) {
                box.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(stringOption.getTooltip())));
            }
            return box;
        }

        if (option instanceof IntegerOption integerOption) {
            EditBox box = new EditBox(this.font, 0, 0, OPTION_EDITOR_WIDTH, ROW_HEIGHT, Component.literal(integerOption.getName()));
            box.setValue(String.valueOf(integerOption.getValue()));
            box.setFilter(text -> text.isEmpty() || text.matches("-?\\d+"));
            box.setResponder(text -> {
                if (!text.isEmpty()) {
                    integerOption.setValue(Integer.parseInt(text));
                }
            });
            if (integerOption.getTooltip() != null) {
                box.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(integerOption.getTooltip())));
            }
            return box;
        }

        if (option instanceof BooleanOption booleanOption) {
            return CycleButton.onOffBuilder(booleanOption.getValue())
                .create(0, 0, OPTION_EDITOR_WIDTH, ROW_HEIGHT, Component.literal(booleanOption.getName()), (button, value) -> booleanOption.setValue(value));
        }

        if (option instanceof EnumOption enumOption && !enumOption.getValues().isEmpty()) {
            return CycleButton.builder(
                Component::literal,
                    enumOption.getValue()
                )
                .withValues(enumOption.getValues())
                .displayOnlyValue()
                .create(0, 0, OPTION_EDITOR_WIDTH, ROW_HEIGHT, Component.literal(enumOption.getName()), (button, value) -> {
                    enumOption.setValue(value);
                    if (PathModePlanner.OPT_PATH_MODE.equals(enumOption.getName())) {
                        refreshOptionList();
                    }
                });
        }

        if (option instanceof BlockOption blockOption) {
            EditBox box = new EditBox(this.font, 0, 0, OPTION_EDITOR_WIDTH, ROW_HEIGHT, Component.literal(blockOption.getName()));
            box.setValue(blockOption.getValue());
            box.setResponder(blockOption::setValue);
            String tooltip = "Enter block ID (e.g., stone, oak_log, minecraft:grass_block)";
            box.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(tooltip)));
            return box;
        }

        if (option instanceof ListOption<?> listOption) {
            return Button.builder(Component.literal("Edit List (" + listOption.size() + " items)"),
                    button -> this.minecraft.setScreen(new ListEditorScreen(listOption, this)))
                .bounds(0, 0, OPTION_EDITOR_WIDTH, ROW_HEIGHT)
                .build();
        }

        if (option instanceof SetOption<?> setOption) {
            return Button.builder(Component.literal("Edit Set (" + setOption.size() + " items)"),
                    button -> this.minecraft.setScreen(new SetEditorScreen(setOption, this)))
                .bounds(0, 0, OPTION_EDITOR_WIDTH, ROW_HEIGHT)
                .build();
        }

        return Button.builder(Component.literal("Unsupported"), button -> {
        }).bounds(0, 0, OPTION_EDITOR_WIDTH, ROW_HEIGHT).build();
    }

    private Component getKeyStrokeLabel(KeyStrokeOption option, boolean capturing) {
        String value = option.getValue();
        String text = value == null || value.isBlank() ? "<unset>" : value;
        if (capturing) {
            return Component.literal("Press keys... (Esc clears)");
        }
        return Component.literal(text);
    }

    private void toggleKeyStrokeCapture(KeyStrokeOption option, Button button) {
        if (this.activeKeyStrokeCapture == option) {
            clearKeyStrokeCapture();
            return;
        }

        clearKeyStrokeCapture();
        this.activeKeyStrokeCapture = option;
        this.activeKeyStrokeButton = button;
        button.setMessage(getKeyStrokeLabel(option, true));
    }

    private void clearKeyStrokeCapture() {
        if (this.activeKeyStrokeCapture != null && this.activeKeyStrokeButton != null) {
            this.activeKeyStrokeButton.setMessage(getKeyStrokeLabel(this.activeKeyStrokeCapture, false));
        }
        this.activeKeyStrokeCapture = null;
        this.activeKeyStrokeButton = null;
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        int keyCode = event.key();
        if (this.activeKeyStrokeCapture != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.activeKeyStrokeCapture.setValue("");
                if (this.activeKeyStrokeCapture == this.keyBindKeyOption) {
                    this.keyBind.setKey("");
                }
                clearKeyStrokeCapture();
                return true;
            }

            if (KeyStrokeOption.isModifierKey(keyCode)) {
                return true;
            }

            String captured = KeyStrokeOption.fromKeyPress(keyCode, event.scancode(), event.modifiers());
            if (!captured.isBlank()) {
                this.activeKeyStrokeCapture.setValue(captured);
                if (this.activeKeyStrokeCapture == this.keyBindKeyOption) {
                    this.keyBind.setKey(captured);
                }
            }
            clearKeyStrokeCapture();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent event) {
        if (this.activeKeyStrokeCapture != null) {
            return true;
        }
        return super.charTyped(event);
    }

    private String formatActionLabel(int index) {
        Action action = this.keyBind.getActions().get(index);
        return (index + 1) + ": " + action.getType().id();
    }

    private static List<ActionType<?>> getActionTypes() {
        return ActionRegistry.getAll().stream()
            .sorted(Comparator.comparing(ActionType::id))
            .toList();
    }

    private static List<ConfigOption<?>> cloneOptions(List<ConfigOption<?>> templates) {
        List<ConfigOption<?>> cloned = new ArrayList<>(templates.size());
        for (ConfigOption<?> option : templates) {
          switch(option) {
            case KeyStrokeOption keyStrokeOption -> cloned.add(new KeyStrokeOption(keyStrokeOption.getName(), keyStrokeOption.getDescription(), keyStrokeOption.getDefaultValue(), keyStrokeOption.getTooltip()));
            case StringOption stringOption -> cloned.add(new StringOption(stringOption.getName(), stringOption.getDescription(), stringOption.getDefaultValue(), stringOption.getTooltip()));
            case BooleanOption booleanOption -> cloned.add(new BooleanOption(booleanOption.getName(), booleanOption.getDescription(), booleanOption.getDefaultValue()));
            case IntegerOption integerOption -> cloned.add(new IntegerOption(integerOption.getName(), integerOption.getDescription(), integerOption.getDefaultValue(), integerOption.getMin(), integerOption.getMax()));
            case EnumOption enumOption -> cloned.add(new EnumOption(enumOption.getName(), enumOption.getDescription(), enumOption.getDefaultValue(), enumOption.getValues()));
            case BlockOption blockOption -> cloned.add(new BlockOption(blockOption.getName(), blockOption.getDescription(), blockOption.getDefaultValue()));
            case ListOption<?> listOption -> {
              List<ConfigOption<?>> clonedItems = new ArrayList<>();
              for(ConfigOption<?> item : listOption.getItems()) {
                clonedItems.add(cloneOptions(List.of(item)).getFirst());
              }
              cloned.add(new ListOption<>(listOption.getName(), listOption.getDescription(), clonedItems));
            }
            case SetOption<?> setOption -> {
              Set<ConfigOption<?>> clonedItems = new LinkedHashSet<>();
              for(ConfigOption<?> item : setOption.getItems()) {
                clonedItems.add(cloneOptions(List.of(item)).getFirst());
              }
              cloned.add(new SetOption<>(setOption.getName(), setOption.getDescription(), clonedItems));
            }
            case null, default -> cloned.add(option);
          }
        }
        return cloned;
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        int x = this.width / 2 - FIELD_WIDTH / 2;
        int columnWidth = (FIELD_WIDTH - COLUMN_GAP) / 2;
        int rightX = x + columnWidth + COLUMN_GAP;
        guiGraphics.drawString(this.font, "Keybind", x, 24, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Description", rightX, 24, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Actions", x, 158, 0xFFFFFF);
        if (this.actionOptionList != null) {
            guiGraphics.drawString(this.font, "Action Options", x, this.actionOptionList.getY() - 10, 0xFFFFFF);
        }
        if (this.showNoActionsHint) {
            guiGraphics.drawString(this.font, Component.literal("No actions yet. Select a type and click Add Action.").withStyle(ChatFormatting.GRAY), x, 202, 0xFFFFFF);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private class ActionOptionList extends ContainerObjectSelectionList<ActionOptionList.OptionEntry> {
        ActionOptionList(net.minecraft.client.Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        void clearAllEntries() {
            this.clearEntries();
        }

        void addOption(ConfigOption<?> option) {
            this.addEntry(new OptionEntry(option));
        }

        @Override
        public int getRowWidth() {
            return FIELD_WIDTH - 8;
        }

        class OptionEntry extends ContainerObjectSelectionList.Entry<OptionEntry> {
            private final String label;
            private final AbstractWidget widget;

            OptionEntry(ConfigOption<?> option) {
                this.label = option.getName() + " (" + option.getType() + ")";
                this.widget = createOptionEditor(option);
            }

            @Override
            public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
                int x = this.getContentX();
                int y = this.getContentY();
                int height = this.getContentHeight();

                guiGraphics.drawString(font, this.label, x, y + (height - font.lineHeight) / 2, 0xFFFFFF);

                this.widget.setX(x + OPTION_LABEL_WIDTH);
                this.widget.setY(y + (height - ROW_HEIGHT) / 2);
                this.widget.setWidth(Math.max(40, this.getContentWidth() - OPTION_LABEL_WIDTH));
                this.widget.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            @Override
            public @NonNull List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
                return List.of(this.widget);
            }

            @Override
            public @NonNull List<? extends net.minecraft.client.gui.narration.NarratableEntry> narratables() {
                return List.of(this.widget);
            }
        }
    }
}