package me.luckyluuk.luckybindings.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;

import java.util.Map;

public class ChestGUIScreenHandler extends ScreenHandler {
  private DefaultedList<ItemStack> items = DefaultedList.ofSize(27, ItemStack.EMPTY);
  private final Map<Integer, ClickHandler> handlers;

  protected ChestGUIScreenHandler(ItemStack[] providedItems, Map<Integer, ClickHandler> handlers) {
    super(null, 0);  // No specific container type, sync ID is 0
    this.handlers = handlers;
    this.items = DefaultedList.ofSize(54, ItemStack.EMPTY);  // 6 rows of 9 slots each

    // Fill the inventory slots with provided items
    for (int i = 0; i < providedItems.length; i++) {
      items.set(i, providedItems[i] == null ? ItemStack.EMPTY : providedItems[i]);
      int x = 8 + (i % 9) * 18;  // Position in the X axis
      int y = 18 + (i / 9) * 18; // Position in the Y axis
      this.addSlot(new DummySlot(items, i, x, y));  // Add the slot with its position
    }
  }

  @Override
  public ItemStack quickMove(PlayerEntity player, int slot) {
    return null;
  }
  @Override
  public boolean canUse(PlayerEntity player) {
    return true;
  }

  public void onSlotClick(int index) {
    ClickHandler handler = handlers.get(index);
    if (handler != null) {
      handler.onClick(index);
    }
  }

  private static class DummySlot extends Slot {
    public DummySlot(DefaultedList<ItemStack> inventory, int index, int x, int y) {
      super(new DummyInventory(inventory), index, x, y);
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
      return false; // Prevent item removal
    }

    @Override
    public boolean canInsert(ItemStack stack) {
      return false; // Prevent item insertion
    }
  }
}

