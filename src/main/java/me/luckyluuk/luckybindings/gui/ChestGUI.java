package me.luckyluuk.luckybindings.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public class ChestGUI {
  private final ItemStack[] items = new ItemStack[54];
  private final Map<Integer, ClickHandler> handlers = new HashMap<>();

  public void setItem(int index, ItemStack stack, ClickHandler handler) {
    items[index] = stack;
    if (handler != null) {
      handlers.put(index, handler);
    }
  }

  public void open(String... args) {
    ChestGUIScreenHandler handler = new ChestGUIScreenHandler(items, handlers);
    ChestGUIScreen screen = new ChestGUIScreen(handler, Text.literal(args.length > 0 ? args[0] : ""));
    MinecraftClient.getInstance().setScreen(screen);
  }

  public void close() {
    MinecraftClient.getInstance().setScreen(null);
  }
}

