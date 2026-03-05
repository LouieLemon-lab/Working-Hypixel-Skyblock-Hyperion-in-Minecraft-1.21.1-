package com.hyperion.mod;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.Holder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import java.util.List;

public class HyperionEvents {

    public static boolean isHyperion(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof HyperionItem;
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof HyperionItem)) return;

        ItemStack stack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        tooltip.clear();

        tooltip.add(stack.getHoverName());
        tooltip.add(Component.empty());

        if (event.getEntity() != null) {
            var enchantments = stack.getAllEnchantments(event.getEntity().registryAccess().lookupOrThrow(Registries.ENCHANTMENT));
            if (!enchantments.isEmpty()) {
                for (var entry : enchantments.entrySet()) {
                    Holder<Enchantment> holder = entry.getKey();
                    int level = entry.getValue();
                    tooltip.add(Enchantment.getFullname(holder, level).copy().withStyle(ChatFormatting.BLUE));
                }
                tooltip.add(Component.empty());
            }
        }

        tooltip.add(Component.literal("Deals ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal("+25%").withStyle(ChatFormatting.RED))
            .append(Component.literal(" damage to Withers.").withStyle(ChatFormatting.GRAY)));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Scroll Abilities:").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("Item Ability: Wither Impact ").withStyle(ChatFormatting.GOLD)
            .append(Component.literal("RIGHT CLICK").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
        tooltip.add(Component.literal("Teleport ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal("10 blocks").withStyle(ChatFormatting.GREEN))
            .append(Component.literal(" ahead of you.").withStyle(ChatFormatting.GRAY)));
        tooltip.add(Component.literal("Then implode dealing a lot of ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal("damage").withStyle(ChatFormatting.RED))
            .append(Component.literal(" to nearby enemies.").withStyle(ChatFormatting.GRAY)));
        tooltip.add(Component.literal("Also applies the ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal("wither shield scroll ability, ").withStyle(ChatFormatting.GRAY))
            .append(Component.literal("reducing damage taken").withStyle(ChatFormatting.GRAY)));
        tooltip.add(Component.literal("and granting an absorption shield for ").withStyle(ChatFormatting.GRAY)
            .append(Component.literal("5 ").withStyle(ChatFormatting.YELLOW))    
            .append(Component.literal("seconds.").withStyle(ChatFormatting.GRAY)));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("MYTHIC DUNGEON ITEM").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
    }

    public static void doWitherImpact(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ItemStack sword = player.getMainHandItem();
        Vec3 playerPos = player.position();
        Vec3 eyePos = playerPos.add(0, player.getEyeHeight(), 0);

        Vec3 lookDir = player.getLookAngle();
        Vec3 lookEnd = eyePos.add(lookDir.scale(10));
        BlockHitResult lookHit = level.clip(new ClipContext(
            eyePos, lookEnd,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            player
        ));

        double finalX, finalY, finalZ;

        if (lookHit.getType() == HitResult.Type.BLOCK) {
            BlockPos hitBlock = lookHit.getBlockPos();
            finalX = hitBlock.getX() + 0.5;
            finalZ = hitBlock.getZ() + 0.5;
            finalY = hitBlock.getY() + 1.0;
        } else {
            finalX = lookEnd.x;
            finalY = lookEnd.y;
            finalZ = lookEnd.z;
            if (player.getXRot() < -45) {
                finalX = playerPos.x;
                finalZ = playerPos.z;
            }
        }

        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4, false, false));
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
            playerPos.x, playerPos.y + 1, playerPos.z, 80, 0.5, 0.8, 0.5, 0.05);

        player.teleportTo(finalX, finalY, finalZ);

        level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
            finalX, finalY + 1, finalZ, 1, 0, 0, 0, 0);
        level.playSound(null, finalX, finalY, finalZ,
            net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
            net.minecraft.sounds.SoundSource.MASTER, 2.0f, 0.8f);

        float baseDamage = 25.0f;
        int sharpness = 0, smite = 0, bane = 0;
        var enchantments = sword.getAllEnchantments(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT));
        for (var entry : enchantments.entrySet()) {
            String key = entry.getKey().unwrapKey().map(k -> k.location().toString()).orElse("");
            int lvl = entry.getValue();
            if (key.equals("minecraft:sharpness")) sharpness = lvl;
            else if (key.equals("minecraft:smite")) smite = lvl;
            else if (key.equals("minecraft:bane_of_arthropods")) bane = lvl;
        }
        float totalDamage = baseDamage + sharpness * baseDamage * 0.05f;
        float smiteBonus = smite * baseDamage * 0.25f;
        float baneBonus = bane * baseDamage * 0.25f;

        AABB box = new AABB(finalX - 7, finalY - 7, finalZ - 7, finalX + 7, finalY + 7, finalZ + 7);
        for (Entity entity : level.getEntities(player, box)) {
            if (entity instanceof LivingEntity living) {
                float dmg = totalDamage;
                if (entity instanceof WitherBoss || entity instanceof WitherSkeleton) dmg *= 1.25f;
                if (living.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) dmg += smiteBonus;
                if (living.getType().is(net.minecraft.tags.EntityTypeTags.ARTHROPOD)) dmg += baneBonus;
                living.hurt(level.damageSources().magic(), dmg);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof WitherBoss) &&
            !(event.getEntity() instanceof WitherSkeleton)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!isHyperion(player.getMainHandItem())) return;
        event.setAmount(event.getAmount() * 1.25f);
    }
}
