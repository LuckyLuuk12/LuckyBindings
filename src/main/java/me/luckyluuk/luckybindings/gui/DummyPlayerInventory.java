package me.luckyluuk.luckybindings.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class DummyPlayerInventory extends PlayerInventory {
  public DummyPlayerInventory() {
    super(MinecraftClient.getInstance().player);
  }

  // prevent crashes from nulls
  @Override
  public ItemStack getMainHandStack() {
    return ItemStack.EMPTY;
  }
}
