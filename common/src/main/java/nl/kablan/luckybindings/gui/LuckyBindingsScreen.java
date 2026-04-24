package nl.kablan.luckybindings.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import nl.kablan.luckybindings.config.ConfigManager;
import nl.kablan.luckybindings.keybinds.KeyBind;
import nl.kablan.luckybindings.keybinds.KeyBindManager;
import org.jspecify.annotations.NonNull;

/**
 * Main GUI for managing LuckyBindings.
 */
public class LuckyBindingsScreen extends Screen {
    private final Screen parent;
    private KeyBindList keyBindList;

    public LuckyBindingsScreen(Screen parent) {
        super(Component.literal("LuckyBindings"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void init() {
        this.keyBindList = new KeyBindList(this.minecraft, this.width, this.height - 64, 32, 25, this::refreshList);
        this.addRenderableWidget(this.keyBindList);

        this.addRenderableWidget(Button.builder(Component.literal("Add New KeyBind"), (button) -> {
            KeyBind kb = new KeyBind("new_key", "New Key Binding", true);
            KeyBindManager.registerDynamic(kb);
            refreshList();
        }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Done"), (button) -> {
            ConfigManager.save();
            this.onClose();
        }).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
        
        refreshList();
    }

    private void refreshList() {
        if (this.keyBindList != null) {
            this.keyBindList.refresh();
        }
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }
}