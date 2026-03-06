package com.hyperion.mod;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class HyperionCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("hyperion")
                .requires(source -> source.hasPermission(0))
                .executes(HyperionCommand::giveHyperion)
        );
    }

    public static ItemStack buildHyperionStack() {
        return new ItemStack(HyperionMod.HYPERION_ITEM.get());
    }

    private static int giveHyperion(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            player.addItem(buildHyperionStack());
            player.sendSystemMessage(Component.literal("You received the ")
                .append(Component.literal("Hyperion").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal("!")));
            return 1;
        } catch (Exception e) {
            HyperionMod.LOGGER.error("Failed to give Hyperion", e);
            return 0;
        }
    }
}
