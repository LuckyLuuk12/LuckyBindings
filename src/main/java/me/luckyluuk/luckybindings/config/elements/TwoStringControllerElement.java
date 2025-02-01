package me.luckyluuk.luckybindings.config.elements;

import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.string.StringControllerElement;
import dev.isxander.yacl3.gui.utils.GuiUtils;
import me.luckyluuk.luckybindings.config.controllers.KeyValueController;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class TwoStringControllerElement extends StringControllerElement {
  protected String inputField2;
  protected Dimension<Integer> inputFieldBounds2;
  protected boolean inputFieldFocused2;
  protected Text emptyText2 = Text.literal("Click to type...").formatted(Formatting.GRAY);

  public TwoStringControllerElement(KeyValueController control, YACLScreen screen, Dimension<Integer> dim, boolean instantApply) {
    super(control, screen, dim, instantApply);
    inputField2 = control.getString2() == null ? "" : control.getString2();
    inputFieldFocused2 = false;
  }

  @Override
  protected void drawValueText(DrawContext graphics, int mouseX, int mouseY, float delta) {
    super.drawValueText(graphics, mouseX, mouseY, delta);

    Text valueText2 = getValueText2();
    if (!isHovered()) valueText2 = Text.literal(GuiUtils.shortenString(valueText2.getString(), textRenderer, getMaxUnwrapLength(), "...")).setStyle(valueText2.getStyle());

    int textX2 = getDimension().xLimit() - textRenderer.getWidth(valueText2) + renderOffset - getXPadding();
    graphics.enableScissor(inputFieldBounds2.x(), inputFieldBounds2.y() - 2, inputFieldBounds2.xLimit() + 1, inputFieldBounds2.yLimit() + 4);
    graphics.drawText(textRenderer, valueText2, textX2, getTextY() + 20, getValueColor(), true); // Adjust Y position for second text field

    if (isHovered()) {
      ticks += delta;

      String text2 = getValueText2().getString();

      graphics.fill(inputFieldBounds2.x(), inputFieldBounds2.yLimit(), inputFieldBounds2.xLimit(), inputFieldBounds2.yLimit() + 1, -1);
      graphics.fill(inputFieldBounds2.x() + 1, inputFieldBounds2.yLimit() + 1, inputFieldBounds2.xLimit() + 1, inputFieldBounds2.yLimit() + 2, 0xFF404040);

      if (inputFieldFocused2 || focused) {
        if (caretPos > text2.length())
          caretPos = text2.length();

        int caretX2 = textX2 + textRenderer.getWidth(text2.substring(0, caretPos));
        if (text2.isEmpty())
          caretX2 = inputFieldBounds2.x() + inputFieldBounds2.width() / 2;

        if (selectionLength != 0) {
          int selectionX2 = textX2 + textRenderer.getWidth(text2.substring(0, caretPos + selectionLength));
          graphics.fill(caretX2, inputFieldBounds2.y() - 2, selectionX2, inputFieldBounds2.yLimit() - 1, 0x803030FF);
        }

        if(caretPos != previousCaretPos) {
          previousCaretPos = caretPos;
          caretTicks = 0;
        }

        if ((caretTicks += delta) % 20 <= 10)
          graphics.fill(caretX2, inputFieldBounds2.y() - 2, caretX2 + 1, inputFieldBounds2.yLimit() - 1, -1);
      }
    }
    graphics.disableScissor();
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (super.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }

    if (isAvailable() && inputFieldBounds2.isPointInside((int) mouseX, (int) mouseY)) {
      inputFieldFocused2 = true;
      caretPos = getDefaultCaretPos();
      selectionLength = 0;
      return true;
    } else {
      inputFieldFocused2 = false;
    }

    return false;
  }

  @Override
  public boolean charTyped(char chr, int modifiers) {
    if (inputFieldFocused2) {
      write2(Character.toString(chr));
      updateUndoHistory();
      return true;
    }

    return super.charTyped(chr, modifiers);
  }

  public void write2(String string) {
    if (selectionLength == 0) {
      if (modifyInput2(builder -> builder.insert(caretPos, string))) {
        caretPos += string.length();
        checkRenderOffset();
      }
    } else {
      int start = getSelectionStart();
      int end = getSelectionEnd();

      if (modifyInput2(builder -> builder.replace(start, end, string))) {
        caretPos = start + string.length();
        selectionLength = 0;
        checkRenderOffset();
      }
    }
  }

  public boolean modifyInput2(Consumer<StringBuilder> consumer) {
    StringBuilder temp = new StringBuilder(inputField2);
    consumer.accept(temp);
    if (!control.isInputValid(temp.toString()))
      return false;
    inputField2 = temp.toString();
    if (instantApply)
      updateControl2();
    return true;
  }

  protected void updateControl2() {
    ((KeyValueController) control).setFromString2(inputField2);
  }

  @Override
  public void setDimension(Dimension<Integer> dim) {
    super.setDimension(dim);

    int width = Math.max(6, Math.min(textRenderer.getWidth(
      getValueText2()),
      getUnshiftedLength()));
    inputFieldBounds2 = Dimension.ofInt(dim.xLimit() - getXPadding() - width, dim.centerY() - textRenderer.fontHeight / 2 + 20, width, textRenderer.fontHeight); // Adjust Y position for second text field
  }

  protected Text getValueText2() {
    if (!inputFieldFocused2 && (inputField2 == null || inputField2.isEmpty())) return emptyText2;
    return instantApply || !inputFieldFocused2 ? ((KeyValueController) control).formatValue2() : Text.literal(inputField2 == null ? "" : inputField2);
  }
}