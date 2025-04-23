package me.luckyluuk.luckybindings.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;

public class ChestGUIScreen extends HandledScreen<ChestGUIScreenHandler> {

  public ChestGUIScreen(ChestGUIScreenHandler handler, Text title) {
    super(handler, new DummyPlayerInventory(), title);
    this.backgroundWidth = 176;
    this.backgroundHeight = 166 * 2;
    // remove the player inventory text by setting the height outside the screen
    this.playerInventoryTitleY = -1000;
  }

  @Override
  protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
    // You can draw a background texture here if you'd like
  }

  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    this.renderBackground(context, mouseX, mouseY, delta); // draw dark background
    super.render(context, mouseX, mouseY, delta);
    this.drawMouseoverTooltip(context, mouseX, mouseY);
  }

  @Override
  protected void handledScreenTick() {
    super.handledScreenTick();
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    boolean result = super.mouseClicked(mouseX, mouseY, button);
    if (focusedSlot != null && focusedSlot.hasStack()) {
      handler.onSlotClick(focusedSlot.getIndex());
    }
    return result;
  }
}

