package me.luckyluuk.luckybindings.actions;

import me.luckyluuk.luckybindings.gui.ChestGUI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.util.*;
import java.util.stream.Collectors;

public class InfoGUI extends Action {
  public InfoGUI(String... args) {
    super("server_info_gui", """
        Opens the server info GUI.
        """);
    setArgs(args);
  }

  @Override
  public void execute() {
    ClientPlayerEntity player = MinecraftClient.getInstance().player;
    if (player == null) return;
    // Open a chest GUI with 3 rows and put items in the middle row for:
    ChestGUI gui = new ChestGUI();
    // 1. Server Ping + Address
    gui.setItem(11, createPingItem(), (slot) -> {});

    // 2. Player Count → Open another ChestGUI with player skulls
    gui.setItem(13, createPlayerCountItem(), (slot) -> {
      if (MinecraftClient.getInstance().getNetworkHandler() == null || MinecraftClient.getInstance().world == null) return;
      openPlayerGUI();
    });

    // 3. Server MOTD (limited on client-side but can show IP or hardcoded description)
    gui.setItem(15, createMOTDItem(), (slot) -> {});

    gui.open();
  }

  @Override
  public void setArgs(String... args) {

  }

  private static final int ITEMS_PER_PAGE = 53;

  private void openPlayerGUI(int... page) {
    int pageOffset = page.length > 0 ? page[0] : 0;
    ChestGUI playerGui = new ChestGUI();
    List<PlayerEntity> players = MinecraftClient.getInstance().getNetworkHandler() == null || MinecraftClient.getInstance().world == null ? new ArrayList<>() :
      MinecraftClient.getInstance().getNetworkHandler().getPlayerList().stream()
        .map(info -> MinecraftClient.getInstance().world.getPlayerByUuid(info.getProfile().getId()))
        .filter(Objects::nonNull) // Filter out null players
        .filter(new HashSet<>()::add) // Remove duplicates
        .filter(p -> MinecraftClient.getInstance().player == null || p.getUuid() != MinecraftClient.getInstance().player.getUuid()) // Remove self
        .toList();

    for (int i = pageOffset * ITEMS_PER_PAGE; i < (pageOffset + 1) * ITEMS_PER_PAGE && i < players.size(); i++) {
      PlayerEntity p = players.get(i);
      ItemStack skull = modifyItem(setSkullOwner(new ItemStack(Items.PLAYER_HEAD), p), "§f"+p.getName().getString(),
        "§eClick to display particle path to player shortly",
        "§7UUID: " + p.getUuid().toString(),
        "§7Location: " + p.getBlockX() + ", " + p.getBlockY() + ", " + p.getBlockZ(),
        "§7Distance: " + Math.round(p.distanceTo(MinecraftClient.getInstance().player) * 100.0) / 100.0 + " blocks");

      int slotIndex = i % 53;
      playerGui.setItem(slotIndex, skull, (s) -> MinecraftClient.getInstance().player.sendMessage(Text.literal("§eFollowing " + p.getName().getString() + " for 5 seconds..."), false));
    }
    // Add a button to go to the next page
    if (players.size() > (pageOffset + 1) * ITEMS_PER_PAGE) {
      ItemStack nextPage = modifyItem(new ItemStack(Items.ARROW), "§6Next Page","§eClick to go to the next page");
      playerGui.setItem(53, nextPage, (s) -> openPlayerGUI(pageOffset + 1));
    }

    playerGui.open();
  }



  private ItemStack createPingItem() {
    ItemStack item = new ItemStack(Items.ENDER_EYE);
    String name = MinecraftClient.getInstance().getCurrentServerEntry() == null ? "" : MinecraftClient.getInstance().getCurrentServerEntry().name;
    long ping = MinecraftClient.getInstance().getCurrentServerEntry() == null ? 0 : MinecraftClient.getInstance().getCurrentServerEntry().ping;
    String addr = MinecraftClient.getInstance().getCurrentServerEntry() == null ? "?" : MinecraftClient.getInstance().getCurrentServerEntry().address;
    String ver = MinecraftClient.getInstance().getCurrentServerEntry() == null ? "?" : MinecraftClient.getInstance().getCurrentServerEntry().version.getString();
    ServerInfo.ServerType type = MinecraftClient.getInstance().getCurrentServerEntry() == null ? null : MinecraftClient.getInstance().getCurrentServerEntry().getServerType();


    return modifyItem(item, "§fServer statistics" + (!name.isBlank() ? " - " +name : ""),
      "§7Ping: " + ping + "ms"
      , "§7Address: " + addr
      , "§7Version: " + ver
      , "§7Server Type: " + (type == null ? "?" : type.toString()));
  }

  private ItemStack createPlayerCountItem() {
    ItemStack item = new ItemStack(Items.PLAYER_HEAD);
    int playerCount = MinecraftClient.getInstance().getNetworkHandler() == null || MinecraftClient.getInstance().world == null ? 0 :
      MinecraftClient.getInstance().getNetworkHandler().getPlayerList().stream()
        .map(info -> MinecraftClient.getInstance().world.getPlayerByUuid(info.getProfile().getId()))
        .filter(Objects::nonNull) // Filter out null players
        .filter(new HashSet<>()::add) // Remove duplicates
        .filter(p -> MinecraftClient.getInstance().player == null || p.getUuid() != MinecraftClient.getInstance().player.getUuid()) // Remove self
        .toList().size();
    return modifyItem(item, "§fPlayers §7("+playerCount+")",
      "§eClick to view players",
      "§cNote: This is a client-side list,",
      "§cit may not be accurate.");
  }

  private ItemStack createMOTDItem() {
    ItemStack item = new ItemStack(Items.BOOK);
    String motd = MinecraftClient.getInstance().getServer() == null ? "?" : MinecraftClient.getInstance().getServer().getServerMotd();
    if (motd == null || motd.isBlank()) motd = "§cNo MOTD set";
    List<String> motdList = Arrays.asList(motd.split("\n"));
    return modifyItem(item, "§fServer MOTD", motdList.toArray(new String[0]));
  }

  private ItemStack modifyItem(ItemStack stack, String name, String... lore) {
    stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
    List<Text> loreList = Arrays.stream(lore).map(Text::literal).collect(Collectors.toList());
    stack.set(DataComponentTypes.LORE, new LoreComponent(loreList));
    return stack;
  }

  private ItemStack setSkullOwner(ItemStack stack, PlayerEntity player) {
    if (stack.getItem() == Items.PLAYER_HEAD) {
      Map<String, String> blockStateMap = new HashMap<>();
      blockStateMap.put("SkullOwner", player.getName().getString());
      blockStateMap.put("profile", player.getName().getString());
      stack.set(DataComponentTypes.BLOCK_STATE, new BlockStateComponent(blockStateMap));
      NbtCompound nbt = new NbtCompound();
      nbt.putString("SkullOwner", player.getName().getString());
      nbt.putString("profile", player.getName().getString());
      nbt.putString("Profile", player.getName().getString());
      stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
    }
    return stack;
  }



}
