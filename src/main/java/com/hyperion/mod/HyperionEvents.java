package com.hyperion.mod;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class HyperionEvents {

    public static boolean isHyperion(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.NETHERITE_SWORD) return false;
        return stack.getHoverName().getString().contains("Hyperion");
    }

    public static void doWitherImpact(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ItemStack sword = player.getMainHandItem();

        Vec3 look = player.getLookAngle();
        Vec3 eyePos = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 targetRaw = eyePos.add(look.scale(10));

        // Raycast to find first block collision
        BlockHitResult hit = level.clip(new ClipContext(
            eyePos, targetRaw,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            player
        ));

        // Find the best safe teleport position by stepping back from the hit
        Vec3 teleportFeet = findSafeTeleportPos(level, player, look, hit, targetRaw);

        Vec3 originPos = player.position().add(0, 1, 0);

        // Wither shield effects
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4, false, false));

        // Origin particles
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
            originPos.x, originPos.y, originPos.z, 80, 0.5, 0.8, 0.5, 0.05);

        // Teleport player
        player.teleportTo(teleportFeet.x, teleportFeet.y, teleportFeet.z);

        // Explosion at destination
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
            teleportFeet.x, teleportFeet.y + 1, teleportFeet.z, 1, 0, 0, 0, 0);
        level.playSound(null, teleportFeet.x, teleportFeet.y, teleportFeet.z,
            net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
            net.minecraft.sounds.SoundSource.MASTER, 2.0f, 0.8f);

        // Calculate enchant bonuses
        float baseDamage = 1000.0f;
        int sharpness = 0, smite = 0, bane = 0;
        var enchantments = sword.getAllEnchantments(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT));
        for (var entry : enchantments.entrySet()) {
            String key = entry.getKey().unwrapKey().map(k -> k.location().toString()).orElse("");
            int lvl = entry.getValue();
            if (key.equals("minecraft:sharpness")) sharpness = lvl;
            else if (key.equals("minecraft:smite")) smite = lvl;
            else if (key.equals("minecraft:bane_of_arthropods")) bane = lvl;
        }
        float sharpnessBonus = sharpness * baseDamage * 0.05f;
        float smiteBonus = smite * baseDamage * 0.25f;
        float baneBonus = bane * baseDamage * 0.25f;
        float totalDamage = baseDamage + sharpnessBonus;

        // Damage nearby entities
        AABB box = new AABB(teleportFeet.x - 8, teleportFeet.y - 8, teleportFeet.z - 8,
                            teleportFeet.x + 8, teleportFeet.y + 8, teleportFeet.z + 8);
        for (Entity entity : level.getEntities(player, box)) {
            if (entity instanceof LivingEntity living) {
                float dmg = totalDamage;
                if (living.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) dmg += smiteBonus;
                if (living.getType().is(net.minecraft.tags.EntityTypeTags.ARTHROPOD)) dmg += baneBonus;
                living.hurt(level.damageSources().magic(), dmg);
            }
        }
    }

    private static Vec3 findSafeTeleportPos(ServerLevel level, ServerPlayer player, Vec3 look, BlockHitResult hit, Vec3 targetRaw) {
        double eyeHeight = player.getEyeHeight();

        // If no block hit, use raw target
        if (hit.getType() != HitResult.Type.BLOCK) {
            return targetRaw.subtract(0, eyeHeight, 0);
        }

        // Step back from the wall in increments until we find a clear spot
        Vec3 hitPos = hit.getLocation();
        for (double stepBack = 0.6; stepBack <= 3.0; stepBack += 0.3) {
            Vec3 candidate = hitPos.subtract(look.scale(stepBack));
            Vec3 candidateFeet = candidate.subtract(0, eyeHeight, 0);

            BlockPos feet = BlockPos.containing(candidateFeet.x, candidateFeet.y, candidateFeet.z);
            BlockPos head = BlockPos.containing(candidateFeet.x, candidateFeet.y + 1.8, candidateFeet.z);

            boolean feetClear = isClear(level, feet);
            boolean headClear = isClear(level, head);

            if (feetClear && headClear) {
                return candidateFeet;
            }
        }

        // Last resort: just stay close to where the player aimed but nudged back
        Vec3 fallback = hitPos.subtract(look.scale(1.5)).subtract(0, eyeHeight, 0);
        return fallback;
    }

    private static boolean isClear(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.isAir()
            || state.getFluidState().is(FluidTags.WATER)
            || state.getFluidState().is(FluidTags.LAVA);
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isHyperion(event.getItemStack())) return;
        event.setCanceled(true);
        doWitherImpact(player);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isHyperion(event.getItemStack())) return;
        event.setCanceled(true);
        doWitherImpact(player);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!isHyperion(event.getItemStack())) return;
        event.setCanceled(true);
        doWitherImpact(player);
    }
}
