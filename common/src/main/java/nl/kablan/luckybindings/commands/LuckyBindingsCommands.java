package nl.kablan.luckybindings.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import nl.kablan.luckybindings.platform.Services;

public class LuckyBindingsCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("luckybindings")
                .then(Commands.literal("config")
                        .executes(context -> {
                            Minecraft.getInstance().execute(() -> {
                                Minecraft.getInstance().setScreen(Services.CONFIG_SCREEN.createConfigScreen(Minecraft.getInstance().screen));
                            });
                            return 1;
                        }))
                .then(Commands.literal("cfg")
                        .executes(context -> {
                            Minecraft.getInstance().execute(() -> {
                                Minecraft.getInstance().setScreen(Services.CONFIG_SCREEN.createConfigScreen(Minecraft.getInstance().screen));
                            });
                            return 1;
                        }));

        dispatcher.register(builder);
        dispatcher.register(Commands.literal("lb").redirect(dispatcher.getRoot().getChild("luckybindings")));
    }
}