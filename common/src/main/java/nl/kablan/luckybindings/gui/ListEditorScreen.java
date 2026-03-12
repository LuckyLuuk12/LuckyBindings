package nl.kablan.luckybindings.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import nl.kablan.luckybindings.config.option.BlockOption;
import nl.kablan.luckybindings.config.option.BooleanOption;
import nl.kablan.luckybindings.config.option.ConfigOption;
import nl.kablan.luckybindings.config.option.EnumOption;
import nl.kablan.luckybindings.config.option.IntegerOption;
import nl.kablan.luckybindings.config.option.KeyStrokeOption;
import nl.kablan.luckybindings.config.option.ListOption;
import nl.kablan.luckybindings.config.option.StringOption;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Screen for editing items in a ListOption.
 * Allows adding, removing, and reordering items.
 */
public class ListEditorScreen extends Screen {
    private static final int FIELD_WIDTH = 400;
    private static final int ROW_HEIGHT = 20;
    private static final int ITEM_ROW_HEIGHT = 26;

    private final ListOption<?> listOption;
    private final Screen parent;
    private ListItemList itemList;

    public ListEditorScreen(ListOption<?> listOption, Screen parent) {
        super(Component.literal("Edit List: " + listOption.getName()));
        this.listOption = listOption;
        this.parent = parent;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void init() {
        int x = this.width / 2 - FIELD_WIDTH / 2;
        int y = 40;

        this.itemList = new ListItemList(this.minecraft, FIELD_WIDTH, this.height - 100, y, ITEM_ROW_HEIGHT);
        this.addRenderableWidget(this.itemList);

        // Refresh the list display
        refreshItemList();

        y = this.height - 50;

        this.addRenderableWidget(Button.builder(Component.literal("Add Item"), (button) -> {
            addNewItem();
            refreshItemList();
        }).bounds(x, y, 100, ROW_HEIGHT).build());

        this.addRenderableWidget(Button.builder(Component.literal("Remove Selected"), (button) -> {
            if (this.itemList.getSelected() != null) {
                removeItem(this.itemList.getSelected());
                refreshItemList();
            }
        }).bounds(x + 110, y, 120, ROW_HEIGHT).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), (button) -> {
            this.onClose();
        }).bounds(x + FIELD_WIDTH - 60, y, 60, ROW_HEIGHT).build());
    }

    private void refreshItemList() {
        if (this.itemList == null) return;

        this.itemList.clearAllEntries();
        for (int i = 0; i < this.listOption.size(); i++) {
            ConfigOption<?> item = this.listOption.getItem(i);
            if (item != null) {
                this.itemList.addItemEntry(new ListItemEntry(item, i));
            }
        }
    }

    private void addNewItem() {
        // Create a default item based on the list's item type
        ConfigOption<?> newItem = createDefaultItem();
        @SuppressWarnings("unchecked")
        ListOption<ConfigOption<?>> list = (ListOption<ConfigOption<?>>) this.listOption;
        list.addItem(newItem);
    }

    private void removeItem(ListItemEntry entry) {
        this.listOption.removeItem(entry.index);
    }

    private ConfigOption<?> createDefaultItem() {
        // Create a default item based on first existing item or make a string
        if (this.listOption.size() > 0) {
            ConfigOption<?> firstItem = this.listOption.getItem(0);
            if (firstItem instanceof BlockOption) {
                return new BlockOption("Block", "", "stone");
            } else if (firstItem instanceof KeyStrokeOption) {
                return new KeyStrokeOption("Key", "", "", "Press combo like CTRL+K; Esc to clear in main editor");
            } else if (firstItem instanceof StringOption) {
                return new StringOption("Item", "", "", null);
            } else if (firstItem instanceof BooleanOption) {
                return new BooleanOption("Item", "", false);
            } else if (firstItem instanceof IntegerOption intOpt) {
                return new IntegerOption("Item", "", 0, intOpt.getMin(), intOpt.getMax());
            }
        }
        return new StringOption("Item", "", "", null);
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    private class ListItemList extends net.minecraft.client.gui.components.ContainerObjectSelectionList<ListItemEntry> {
        ListItemList(net.minecraft.client.Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        /** Wrapper so the outer class can call the protected addEntry() without a cross-package access issue. */
        void addItemEntry(ListItemEntry entry) {
            this.addEntry(entry);
        }

        void clearAllEntries() {
            this.clearEntries();
        }

        @Override
        public int getRowWidth() {
            return FIELD_WIDTH - 8;
        }
    }

    private class ListItemEntry extends net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry<ListItemEntry> {
        private final ConfigOption<?> item;
        private final int index;
        private final AbstractWidget editorWidget;
        private final Button upButton;
        private final Button downButton;

        ListItemEntry(ConfigOption<?> item, int index) {
            this.item = item;
            this.index = index;
            this.editorWidget = createItemEditor(item);
            this.upButton = Button.builder(Component.literal("↑"), (btn) -> {
                if (index > 0) {
                    listOption.moveItem(index, index - 1);
                    refreshItemList();
                }
            }).bounds(0, 0, 30, ROW_HEIGHT).build();

            this.downButton = Button.builder(Component.literal("↓"), (btn) -> {
                if (index < listOption.size() - 1) {
                    listOption.moveItem(index, index + 1);
                    refreshItemList();
                }
            }).bounds(0, 0, 30, ROW_HEIGHT).build();
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int x = this.getContentX();
            int y = this.getContentY();
            int height = this.getContentHeight();

            String label = (index + 1) + ".";
            guiGraphics.drawString(font, label, x + 5, y + (height - font.lineHeight) / 2, 0xFFFFFF);

            int editorX = x + 24;
            int editorY = y + (height - ROW_HEIGHT) / 2;
            int editorWidth = Math.max(80, this.getContentWidth() - 24 - 76);

            this.editorWidget.setX(editorX);
            this.editorWidget.setY(editorY);
            this.editorWidget.setWidth(editorWidth);
            this.editorWidget.render(guiGraphics, mouseX, mouseY, partialTick);

            int buttonX = x + this.getContentWidth() - 70;
            int buttonY = y + (height - ROW_HEIGHT) / 2;

            this.upButton.setX(buttonX);
            this.upButton.setY(buttonY);
            this.upButton.render(guiGraphics, mouseX, mouseY, partialTick);

            this.downButton.setX(buttonX + 35);
            this.downButton.setY(buttonY);
            this.downButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public @NonNull List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
            return List.of(this.editorWidget, this.upButton, this.downButton);
        }

        @Override
        public @NonNull List<? extends net.minecraft.client.gui.narration.NarratableEntry> narratables() {
            return List.of(this.editorWidget, this.upButton, this.downButton);
        }

        private AbstractWidget createItemEditor(ConfigOption<?> option) {
            if (option instanceof BlockOption blockOption) {
                EditBox box = new EditBox(font, 0, 0, 120, ROW_HEIGHT, Component.literal(blockOption.getName()));
                box.setValue(blockOption.getValue());
                box.setResponder(blockOption::setValue);
                box.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                        Component.literal("Enter block ID (e.g., stone, oak_log, minecraft:grass_block)")));
                return box;
            }

            if (option instanceof KeyStrokeOption keyStrokeOption) {
                EditBox box = new EditBox(font, 0, 0, 120, ROW_HEIGHT, Component.literal(keyStrokeOption.getName()));
                box.setValue(keyStrokeOption.getValue());
                box.setResponder(keyStrokeOption::setValue);
                box.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                        Component.literal("Combo format: CTRL+SHIFT+K")));
                return box;
            }

            if (option instanceof StringOption stringOption) {
                EditBox box = new EditBox(font, 0, 0, 120, ROW_HEIGHT, Component.literal(stringOption.getName()));
                box.setValue(stringOption.getValue());
                box.setResponder(stringOption::setValue);
                if (stringOption.getTooltip() != null) {
                    box.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(stringOption.getTooltip())));
                }
                return box;
            }

            if (option instanceof IntegerOption integerOption) {
                EditBox box = new EditBox(font, 0, 0, 120, ROW_HEIGHT, Component.literal(integerOption.getName()));
                box.setValue(String.valueOf(integerOption.getValue()));
                box.setFilter(text -> text.isEmpty() || text.matches("-?\\d+"));
                box.setResponder(text -> {
                    if (!text.isEmpty()) {
                        integerOption.setValue(Integer.parseInt(text));
                    }
                });
                return box;
            }

            if (option instanceof BooleanOption booleanOption) {
                return CycleButton.onOffBuilder(booleanOption.getValue())
                        .create(0, 0, 120, ROW_HEIGHT, Component.literal(booleanOption.getName()),
                                (button, value) -> booleanOption.setValue(value));
            }

            if (option instanceof EnumOption enumOption && !enumOption.getValues().isEmpty()) {
                return CycleButton.builder(Component::literal, enumOption.getValue())
                        .withValues(enumOption.getValues())
                        .displayOnlyValue()
                        .create(0, 0, 120, ROW_HEIGHT, Component.literal(enumOption.getName()),
                                (button, value) -> enumOption.setValue(value));
            }

            return Button.builder(Component.literal("Unsupported"), button -> { }).bounds(0, 0, 120, ROW_HEIGHT).build();
        }
    }
}