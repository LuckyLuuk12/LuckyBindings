package me.luckyluuk.luckybindings.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class DummyInventory implements Inventory {
  private final DefaultedList<ItemStack> items;

  public DummyInventory(DefaultedList<ItemStack> items) {
    this.items = items;
  }

  @Override public int size() { return items.size(); }
  @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
  @Override public ItemStack getStack(int slot) { return items.get(slot); }
  @Override public ItemStack removeStack(int slot, int amount) { return ItemStack.EMPTY; }
  @Override public ItemStack removeStack(int slot) { return ItemStack.EMPTY; }
  @Override public void setStack(int slot, ItemStack stack) { items.set(slot, stack); }
  @Override public void markDirty() {}
  @Override public boolean canPlayerUse(PlayerEntity player) { return true; }
  @Override public void clear() { items.clear(); }
}

