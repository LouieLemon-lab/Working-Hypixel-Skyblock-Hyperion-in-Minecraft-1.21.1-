package com.hyperion.mod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;

public class HyperionItem extends FishingRodItem {
    public HyperionItem() {
        super(new Properties().durability(64));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 22;
    }

    @Override
public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipData, TooltipFlag tooltipFlag) {
    tooltipData.add(Component.literal("Deals +50% damage to Withers.").withStyle(ChatFormatting.RED));
    tooltipData.add(Component.empty());
    tooltipData.add(Component.literal("Scroll Abilities:").withStyle(ChatFormatting.GREEN));
    tooltipData.add(Component.literal("Item Ability: Wither Impact ").withStyle(ChatFormatting.GOLD)
        .append(Component.literal("RIGHT CLICK").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
    tooltipData.add(Component.literal("Teleport 10 blocks ahead of you. Then implode dealing ").withStyle(ChatFormatting.GRAY)
        .append(Component.literal("25 damage").withStyle(ChatFormatting.RED))
        .append(Component.literal(" to nearby enemies. Also applies the wither shield scroll ability reducing damage taken and granting an absorption shield for ").withStyle(ChatFormatting.GRAY))
        .append(Component.literal("5 seconds").withStyle(ChatFormatting.RED))
        .append(Component.literal(".").withStyle(ChatFormatting.GRAY)));
    tooltipData.add(Component.empty());
    tooltipData.add(Component.literal("\u2605 MYTHIC DUNGEON ITEM \u2605").withStyle(ChatFormatting.LIGHT_PURPLE));
}
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(player.getItemInHand(hand));
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            HyperionEvents.doWitherImpact(serverPlayer);
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
