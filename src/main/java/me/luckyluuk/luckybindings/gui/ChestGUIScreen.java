package me.luckyluuk.luckybindings.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class ChestGUIScreen extends HandledScreen<ChestGUIScreenHandler> {
  private static final Identifier CHEST_TEXTURE = Identifier.tryParse("minecraft", "textures/gui/container/generic_54.png");


  public ChestGUIScreen(ChestGUIScreenHandler handler, Text title) {
    super(handler, new DummyPlayerInventory(), title);
    this.backgroundWidth = 176;
    this.backgroundHeight = 166 * 2;
    // remove the player inventory text by setting the height outside the screen
    this.playerInventoryTitleY = -1000;
  }

  @Override
  protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
    // Attempt to draw a proper chest gui... no success yet... ):
    int x = (width - backgroundWidth) / 2;
    int y = (height - backgroundHeight) / 2;

    // Draw the chest GUI texture
    context.drawTexture(
      RenderLayer::getGuiOpaqueTexturedBackground,
      CHEST_TEXTURE,
      x, y,          // screen position
      0, 0,          // texture position (u, v)
      backgroundWidth, backgroundHeight,  // size on screen
      backgroundWidth, backgroundHeight,  // region size in texture
      256, 256,      // total texture size (Minecraft textures are typically 256x256)
      0xFFFFFF       // no tinting

    );


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
    if (focusedSlot != null && focusedSlot.hasStack()) {
      handler.onSlotClick(focusedSlot.getIndex());
    }
    return true;
  }
}

