package nl.kablan.luckybindings.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import nl.kablan.luckybindings.keybinds.KeyBind;
import nl.kablan.luckybindings.keybinds.KeyBindManager;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class KeyBindList extends ContainerObjectSelectionList<KeyBindList.KeyBindEntry> {
    private final Runnable onChanged;

    public KeyBindList(Minecraft minecraft, int width, int height, int y, int itemHeight, Runnable onChanged) {
        super(minecraft, width, height, y, itemHeight);
        this.onChanged = onChanged;
    }

    public void refresh() {
        this.clearEntries();
        for (KeyBind keyBind : KeyBindManager.getActiveKeyBinds()) {
            this.addEntry(new KeyBindEntryImpl(keyBind, this.onChanged));
        }
    }

    @Override
    public int getRowWidth() {
        return 380;
    }

    public abstract static class KeyBindEntry extends ContainerObjectSelectionList.Entry<KeyBindEntry> {
        protected final KeyBind keyBind;
        protected final Button editButton;
        protected final Button enabledButton;
        protected final Button removeButton;
        protected final EditBox keyNameField;

        public KeyBindEntry(KeyBind keyBind, Runnable onChanged) {
            this.keyBind = keyBind;
            this.editButton = Button.builder(Component.literal("Edit"), button ->
                Minecraft.getInstance().setScreen(new KeyBindEditorScreen(keyBind, Minecraft.getInstance().screen))
            ).bounds(0, 0, 50, 20).build();

            this.enabledButton = Button.builder(getEnabledLabel(keyBind), (button) -> {
                keyBind.setEnabled(!keyBind.isEnabled());
                button.setMessage(getEnabledLabel(keyBind));
            }).bounds(0, 0, 42, 20).build();

            boolean isDynamic = KeyBindManager.isDynamic(keyBind);
            this.removeButton = Button.builder(Component.literal("X"), button -> {
                if (KeyBindManager.removeDynamic(keyBind)) onChanged.run();
            }).bounds(0, 0, 22, 20).build();
            this.removeButton.active = isDynamic;
            this.removeButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(getRemoveTooltip(keyBind, isDynamic)));

            this.keyNameField = new EditBox(Minecraft.getInstance().font, 0, 0, 120, 20, Component.literal("Key Name"));
            this.keyNameField.setValue(keyBind.getKey());
            this.keyNameField.setMaxLength(80);
            this.keyNameField.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                Component.literal("Type the key combo directly, or click Edit to capture it by pressing the keys.")
            ));
            this.keyNameField.setResponder(value -> {
                String trimmed = value.trim();
                keyBind.setKey(trimmed);
            });
        }

        private static Component getEnabledLabel(KeyBind keyBind) {
            return Component.literal(keyBind.isEnabled() ? "On" : "Off");
        }

        protected static Component getRemoveTooltip(KeyBind keyBind, boolean isDynamic) {
            if (!isDynamic) {
                return Component.literal("Only dynamic keybinds can be removed");
            }

            String key = keyBind.getKey();
            String displayKey = key == null || key.isBlank() ? "<unset keybind>" : key;
            return Component.literal("Click to remove " + displayKey);
        }

        @Override
        public @NonNull List<? extends GuiEventListener> children() {
            return List.of(keyNameField, enabledButton, removeButton, editButton);
        }

        @Override
        public @NonNull List<? extends NarratableEntry> narratables() {
            return List.of(keyNameField, enabledButton, removeButton, editButton);
        }
    }

    public static class KeyBindEntryImpl extends KeyBindEntry {
        public KeyBindEntryImpl(KeyBind keyBind, Runnable onChanged) {
            super(keyBind, onChanged);
        }

        @Override
        public void renderContent(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int x = this.getContentX();
            int y = this.getContentY();
            int contentWidth = this.getContentWidth();
            int contentHeight = this.getContentHeight();

            int buttonY = y + (contentHeight - 20) / 2;
            int editX = x + contentWidth - 52;
            int removeX = editX - 26;
            int toggleX = removeX - 46;

            int keyFieldWidth = Math.max(90, toggleX - x - 8);
            this.keyNameField.setX(x);
            this.keyNameField.setY(buttonY);
            this.keyNameField.setWidth(keyFieldWidth);
            this.keyNameField.render(guiGraphics, mouseX, mouseY, partialTick);

            String description = keyBind.getDescription() == null || keyBind.getDescription().isBlank() ? "No description" : keyBind.getDescription();
            var font = Minecraft.getInstance().font;

            if (hovered) {
                List<Component> tooltipLines = List.of(
                    Component.literal("Key: " + keyBind.getKey()),
                    Component.literal("Description: " + description),
                    Component.literal("Actions: " + keyBind.getActions().size())
                );
                guiGraphics.setComponentTooltipForNextFrame(font, tooltipLines, mouseX, mouseY);
            }

            this.enabledButton.setMessage(Component.literal(keyBind.isEnabled() ? "On" : "Off"));
            this.enabledButton.setX(toggleX);
            this.enabledButton.setY(buttonY);
            this.enabledButton.render(guiGraphics, mouseX, mouseY, partialTick);

            this.removeButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(
                getRemoveTooltip(this.keyBind, this.removeButton.active)
            ));
            this.removeButton.setX(removeX);
            this.removeButton.setY(buttonY);
            this.removeButton.render(guiGraphics, mouseX, mouseY, partialTick);

            this.editButton.setX(editX);
            this.editButton.setY(buttonY);
            this.editButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
}