package com.hyperion.mod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import java.util.List;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class HyperionItem extends FishingRodItem {

    public HyperionItem() {
    super(new Properties().durability(64));
}

    @Override
public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipData, TooltipFlag tooltipFlag) {
    tooltipData.add(Component.literal("✦ Wither Impact").withStyle(ChatFormatting.DARK_PURPLE));
    tooltipData.add(Component.literal("Right-click to unleash a devastating").withStyle(ChatFormatting.GRAY));
    tooltipData.add(Component.literal("Wither explosion around you!").withStyle(ChatFormatting.GRAY));
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
