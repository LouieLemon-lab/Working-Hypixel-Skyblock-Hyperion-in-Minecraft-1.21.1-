package com.hyperion.mod;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.List;

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
        ItemStack stack = new ItemStack(HyperionMod.HYPERION_ITEM.get());

        stack.set(DataComponents.CUSTOM_NAME,
            Component.literal("Heroic Hyperion ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withItalic(false))
                .append(Component.literal("✪✪✪✪✪")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withItalic(false)))
        );

        List<Component> loreLines = List.of(
            Component.literal("Item Ability: Wither Impact ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withItalic(false))
                .append(Component.literal("RIGHT CLICK")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true).withItalic(false))),
            Component.literal("Teleport ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                .append(Component.literal("10 blocks")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false)))
                .append(Component.literal(" ahead of you.")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))),
            Component.literal("Then implode dealing damage to nearby enemies.")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false)),
            Component.literal("Applies wither shield reducing damage taken")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false)),
            Component.literal("and granting absorption shield for ")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                .append(Component.literal("5")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(false)))
                .append(Component.literal(" seconds.")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))),
            Component.literal(""),
            Component.literal("Deals +")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                .append(Component.literal("50%")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withItalic(false)))
                .append(Component.literal(" damage to Withers.")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false)))
        );

        stack.set(DataComponents.LORE, new ItemLore(loreLines));

        return stack;
    }

    private static int giveHyperion(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            player.addItem(buildHyperionStack());
            player.sendSystemMessage(Component.literal("§6You received the §dHeroic Hyperion§6!"));
            return 1;
        } catch (Exception e) {
            HyperionMod.LOGGER.error("Failed to give Hyperion", e);
            return 0;
        }
    }
}
