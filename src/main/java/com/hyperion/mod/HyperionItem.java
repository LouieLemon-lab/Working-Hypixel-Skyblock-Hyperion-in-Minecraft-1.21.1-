package com.hyperion.mod;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.event.EventHooks;

public class HyperionItem extends SwordItem {
    public HyperionItem() {
        super(Tiers.NETHERITE, new Properties()
            .durability(64)
            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 20, -2.4f)));
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
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void onCraftedBy(ItemStack stack, net.minecraft.world.level.Level level, net.minecraft.world.entity.player.Player player) {
        super.onCraftedBy(stack, level, player);
        addDefaultEnchantments(stack, level);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        return stack;
    }

    private static void addDefaultEnchantments(ItemStack stack, net.minecraft.world.level.Level level) {
        var registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        var looting = registry.get(net.minecraft.resources.ResourceKey.create(Registries.ENCHANTMENT, 
            net.minecraft.resources.ResourceLocation.withDefaultNamespace("looting")));
        looting.ifPresent(l -> stack.enchant(l, 3));
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
