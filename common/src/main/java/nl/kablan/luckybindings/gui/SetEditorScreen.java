package nl.kablan.luckybindings.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import nl.kablan.luckybindings.config.option.BlockOption;
import nl.kablan.luckybindings.config.option.BooleanOption;
import nl.kablan.luckybindings.config.option.ConfigOption;
import nl.kablan.luckybindings.config.option.IntegerOption;
import nl.kablan.luckybindings.config.option.SetOption;
import nl.kablan.luckybindings.config.option.StringOption;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Screen for editing items in a SetOption.
 * Similar to ListEditorScreen but for unordered sets (no reordering).
 */
public class SetEditorScreen extends Screen {
    private static final int FIELD_WIDTH = 400;
    private static final int ROW_HEIGHT = 20;
    private static final int ITEM_ROW_HEIGHT = 25;

    private final SetOption<?> setOption;
    private final Screen parent;
    private SetItemList itemList;

    public SetEditorScreen(SetOption<?> setOption, Screen parent) {
        super(Component.literal("Edit Set: " + setOption.getName()));
        this.setOption = setOption;
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

        this.itemList = new SetItemList(this.minecraft, FIELD_WIDTH, this.height - 100, y, ITEM_ROW_HEIGHT);
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
        int i = 0;
        for (ConfigOption<?> item : this.setOption.getItems()) {
            this.itemList.addItemEntry(new SetItemEntry(item, i++));
        }
    }

    private void addNewItem() {
        // Create a default item based on the set's item type
        ConfigOption<?> newItem = createDefaultItem();
        if (newItem != null) {
            @SuppressWarnings("unchecked")
            SetOption<ConfigOption<?>> set = (SetOption<ConfigOption<?>>) this.setOption;
            set.addItem(newItem);
        }
    }

    private void removeItem(SetItemEntry entry) {
        @SuppressWarnings("unchecked")
        SetOption<ConfigOption<?>> set = (SetOption<ConfigOption<?>>) this.setOption;
        set.removeItem(entry.item);
    }

    private ConfigOption<?> createDefaultItem() {
        // Create a default item based on first existing item or make a string
        if (this.setOption.size() > 0) {
            ConfigOption<?> firstItem = new java.util.ArrayList<>(this.setOption.getItems()).get(0);
            if (firstItem instanceof BlockOption) {
                return new BlockOption("Block", "", "stone");
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

    private class SetItemList extends net.minecraft.client.gui.components.ContainerObjectSelectionList<SetItemEntry> {
        SetItemList(net.minecraft.client.Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        /** Wrapper so the outer class can call the protected addEntry() without a cross-package access issue. */
        void addItemEntry(SetItemEntry entry) {
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

    private class SetItemEntry extends net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry<SetItemEntry> {
        private final ConfigOption<?> item;
        private final int index;

        SetItemEntry(ConfigOption<?> item, int index) {
            this.item = item;
            this.index = index;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int x = this.getContentX();
            int y = this.getContentY();
            int height = this.getContentHeight();

            // Display item name and type
            String label = (index + 1) + ": " + item.getName() + " (" + item.getType() + ")";
            guiGraphics.drawString(font, label, x + 5, y + (height - font.lineHeight) / 2, 0xFFFFFF);
        }

        @Override
        public @NonNull List<? extends net.minecraft.client.gui.components.events.GuiEventListener> children() {
            return List.of();
        }

        @Override
        public @NonNull List<? extends net.minecraft.client.gui.narration.NarratableEntry> narratables() {
            return List.of();
        }
    }
}