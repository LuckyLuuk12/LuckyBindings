package me.luckyluuk.luckybindings.actions;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import java.text.DecimalFormat;

public class ExecuteCommand extends Action {
  private String command;

  public ExecuteCommand(String... args) {
    super("execute_command", """
    Executes a command as the player. The command can use the following
    placeholders:
    - %main_hand%: The name of the item in the player's main hand.
    - %off_hand%: The name of the item in the player's off hand.
    - %player%: The name of the player.
    - %player_display_name%: The display name of the player.
    - %x%: The x-coordinate of the player.
    - %y%: The y-coordinate of the player.
    - %z%: The z-coordinate of the player.
    - %yaw%: The yaw of the player.
    - %pitch%: The pitch of the player.
    - %head_yaw%: The head yaw of the player.
    - %target%: The name of the entity the player is looking at.
    - %target_x%: The x-coordinate of the entity the player is looking at.
    - %target_y%: The y-coordinate of the entity the player is looking at.
    - %target_z%: The z-coordinate of the entity the player is looking at.
    - %target_yaw%: The yaw of the entity the player is looking at.
    - %target_pitch%: The pitch of the entity the player is looking at.
    - %target_head_yaw%: The head yaw of the entity the player is looking at.
    - %target_display_name%: The display name of the entity the player is looking at.
    """);
    setArgs(args);
  }

  @Override
  public void setArgs(String... args) {
    this.command = args.length > 0 ? args[0] : "";
  }

  @Override
  public void execute() {
    ClientPlayerEntity p = MinecraftClient.getInstance().player;
    if (p == null) return;
    p.networkHandler.sendCommand(parse(command, p));
  }

  static public String parse(String command, ClientPlayerEntity p) {
    DecimalFormat df = new DecimalFormat("#.##");
    if(command.contains("%main_hand%") && p.getMainHandStack().getCustomName()  != null) {
      command = command.replaceAll("%main_hand%", p.getMainHandStack().getCustomName().getString());
    }
    if(command.contains("%off_hand%") && p.getOffHandStack().getCustomName() != null) {
      command = command.replaceAll("%off_hand%", p.getOffHandStack().getCustomName().getString());
    }
    if(command.contains("%player%")) command = command.replaceAll("%player%", p.getName().getString());
    if(command.contains("%player_display_name%") && p.getDisplayName() != null) command = command.replaceAll("%player_display_name%", p.getDisplayName().getString());
    if(command.contains("%x%")) command = command.replaceAll("%x%", df.format(p.getX()));
    if(command.contains("%y%")) command = command.replaceAll("%y%", df.format(p.getY()));
    if(command.contains("%z%")) command = command.replaceAll("%z%", df.format(p.getZ()));
    if(command.contains("%yaw%")) command = command.replaceAll("%yaw%", df.format(p.getYaw()));
    if(command.contains("%pitch%")) command = command.replaceAll("%pitch%", df.format(p.getPitch()));
    if(command.contains("%head_yaw%")) command = command.replaceAll("%head_yaw%", df.format(p.getHeadYaw()));
    if(command.contains("%target%")) {
      HitResult hr = p.raycast(20, 0, false);
      if(hr instanceof EntityHitResult ehr) {
        command = command.replaceAll("%target%", ehr.getEntity().getName().getString());
      }
    }
    if(command.contains("%target_x%")) {
      HitResult hr = p.raycast(20, 0, false);
      if(hr instanceof EntityHitResult ehr) {
        command = command.replaceAll("%target_x%", df.format(ehr.getEntity().getX()));
      }
    }
    if(command.contains("%target_y%")) {
      HitResult hr = p.raycast(20, 0, false);
      if(hr instanceof EntityHitResult ehr) {
        command = command.replaceAll("%target_y%", df.format(ehr.getEntity().getY()));
      }
    }
    if(command.contains("%target_z%")) {
      HitResult hr = p.raycast(20, 0, false);
      if(hr instanceof EntityHitResult ehr) {
        command = command.replaceAll("%target_z%", df.format(ehr.getEntity().getZ()));
      }
    }
    if(command.contains("%target_yaw%")) {
      HitResult hr = p.raycast(20, 0, false);
      if(hr instanceof EntityHitResult ehr) {
        command = command.replaceAll("%target_yaw%", df.format(ehr.getEntity().getYaw()));
      }
    }
    if(command.contains("%target_pitch%")) {
      HitResult hr = p.raycast(20, 0, false);
      if(hr instanceof EntityHitResult ehr) {
        command = command.replaceAll("%target_pitch%", df.format(ehr.getEntity().getPitch()));
      }
    }
    if(command.contains("%target_head_yaw%")) {
      HitResult hr = p.raycast(20, 0, false);
      if(hr instanceof EntityHitResult ehr) {
        command = command.replaceAll("%target_head_yaw%", df.format(ehr.getEntity().getHeadYaw()));
      }
    }
    if(command.contains("%target_display_name%")) {
      HitResult hr = p.raycast(20, 0, false);
      if(hr instanceof EntityHitResult ehr) {
        command = command.replaceAll("%target_display_name%", ehr.getEntity().getName().getString());
      }
    }
    return command;
  }

  @Override
  public String toString() {
    return "ExecuteCommand{" +
      "command='" + command + '\'' +
      '}';
  }
}
