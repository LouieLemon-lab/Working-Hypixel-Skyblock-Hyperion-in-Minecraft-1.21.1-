package com.hyperion.mod;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class HyperionEvents {

    public static boolean isHyperion(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof HyperionItem;
    }

    public static void doWitherImpact(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ItemStack sword = player.getMainHandItem();
        Vec3 playerPos = player.position();
        Vec3 eyePos = playerPos.add(0, player.getEyeHeight(), 0);

        // Raycast in exact look direction
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
            // Looking at a block - place on top of it, centered
            BlockPos hitBlock = lookHit.getBlockPos();
            finalX = hitBlock.getX() + 0.5;
            finalZ = hitBlock.getZ() + 0.5;
            finalY = hitBlock.getY() + 1.0;
        } else {
            // Looking at air - teleport to exact end of raycast
            finalX = lookEnd.x;
            finalY = lookEnd.y;
            finalZ = lookEnd.z;
    
            // If looking steeply upward, clamp horizontal movement
            if (player.getXRot() < -45) {
            finalX = playerPos.x;
            finalZ = playerPos.z;
    }
}
        // Effects at origin
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

        // AOE damage
        float baseDamage = 25.0f;
        int sharpness = 0, smite = 0, bane = 0;
        var enchantments = sword.getAllEnchantments(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT));
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

        AABB box = new AABB(finalX - 8, finalY - 8, finalZ - 8, finalX + 8, finalY + 8, finalZ + 8);
        for (Entity entity : level.getEntities(player, box)) {
            if (entity instanceof LivingEntity living) {
                float dmg = totalDamage;
                if (entity instanceof WitherBoss) dmg *= 1.5f;
                if (living.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) dmg += smiteBonus;
                if (living.getType().is(net.minecraft.tags.EntityTypeTags.ARTHROPOD)) dmg += baneBonus;
                living.hurt(level.damageSources().magic(), dmg);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof WitherBoss)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!isHyperion(player.getMainHandItem())) return;
        event.setAmount(event.getAmount() * 1.5f);
    }
}
