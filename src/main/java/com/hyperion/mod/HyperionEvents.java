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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

public class HyperionEvents {

    public static boolean isHyperion(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.NETHERITE_SWORD) return false;
        Component name = stack.getHoverName();
        return name.getString().contains("Hyperion");
    }

    public static void doWitherImpact(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ItemStack sword = player.getMainHandItem();

        Vec3 look = player.getLookAngle();
        Vec3 eyePos = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 targetRaw = eyePos.add(look.scale(10));

        // Raycast to find first block collision
        BlockHitResult hit = level.clip(new ClipContext(
            eyePos,
            targetRaw,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            player
        ));

        Vec3 teleportEyePos;
        if (hit.getType() == HitResult.Type.BLOCK) {
            // Stop 0.6 blocks before the hit surface so player doesn't clip in
            teleportEyePos = hit.getLocation().subtract(look.scale(0.6));
        } else {
            teleportEyePos = targetRaw;
        }

        // Convert eye position to feet position
        Vec3 teleportPos = new Vec3(teleportEyePos.x, teleportEyePos.y - player.getEyeHeight(), teleportEyePos.z);

        // Check that destination feet and head positions are not inside a block
        BlockPos feetBlock = BlockPos.containing(teleportPos.x, teleportPos.y, teleportPos.z);
        BlockPos headBlock = BlockPos.containing(teleportPos.x, teleportPos.y + 1.8, teleportPos.z);

        BlockState feetState = level.getBlockState(feetBlock);
        BlockState headState = level.getBlockState(headBlock);

        // Only teleport if destination is air, water or lava (not solid)
        boolean feetClear = feetState.isAir() || feetState.getFluidState().is(FluidTags.WATER) || feetState.getFluidState().is(FluidTags.LAVA);
        boolean headClear = headState.isAir() || headState.getFluidState().is(FluidTags.WATER) || headState.getFluidState().is(FluidTags.LAVA);

        if (!feetClear || !headClear) {
            // Try to find a safe spot by stepping back further
            teleportEyePos = hit.getLocation().subtract(look.scale(1.2));
            teleportPos = new Vec3(teleportEyePos.x, teleportEyePos.y - player.getEyeHeight(), teleportEyePos.z);
            feetBlock = BlockPos.containing(teleportPos.x, teleportPos.y, teleportPos.z);
            headBlock = BlockPos.containing(teleportPos.x, teleportPos.y + 1.8, teleportPos.z);
            feetState = level.getBlockState(feetBlock);
            headState = level.getBlockState(headBlock);
            feetClear = feetState.isAir() || feetState.getFluidState().is(FluidTags.WATER) || feetState.getFluidState().is(FluidTags.LAVA);
            headClear = headState.isAir() || headState.getFluidState().is(FluidTags.WATER) || headState.getFluidState().is(FluidTags.LAVA);

            // If still not clear, don't teleport at all
            if (!feetClear || !headClear) {
                player.sendSystemMessage(Component.literal("§cNo clear space to teleport to!"));
                return;
            }
        }

        // Calculate bonus damage from enchantments
        float baseDamage = 1000.0f;
        int sharpness = 0;
        int smite = 0;
        int bane = 0;

        // Get enchantment levels
        var enchantments = sword.getAllEnchantments(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT));
        for (var entry : enchantments.entrySet()) {
            String key = entry.getKey().unwrapKey().map(k -> k.location().toString()).orElse("");
            int lvl = entry.getValue();
            if (key.equals("minecraft:sharpness")) sharpness = lvl;
            else if (key.equals("minecraft:smite")) smite = lvl;
            else if (key.equals("minecraft:bane_of_arthropods")) bane = lvl;
        }

        float sharpnessBonus = sharpness * 0.5f * baseDamage * 0.1f;
        float smiteBonus = smite * 2.5f * baseDamage * 0.1f;
        float baneBonus = bane * 2.5f * baseDamage * 0.1f;
        float totalDamage = baseDamage + sharpnessBonus;

        Vec3 originPos = player.position().add(0, 1, 0);

        // Wither shield effects
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4, false, false));

        // Origin particles only
        level.sendParticles(
            net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
            originPos.x, originPos.y, originPos.z,
            80, 0.5, 0.8, 0.5, 0.05
        );

        // Teleport player
        player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);

        // Explosion particles + sound at destination only
        level.sendParticles(
            net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
            teleportPos.x, teleportPos.y + 1, teleportPos.z,
            1, 0, 0, 0, 0
        );
        level.playSound(null, teleportPos.x, teleportPos.y, teleportPos.z,
            net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
            net.minecraft.sounds.SoundSource.MASTER, 2.0f, 0.8f);

        // Damage nearby entities with enchant bonuses
        AABB box = new AABB(
            teleportPos.x - 8, teleportPos.y - 8, teleportPos.z - 8,
            teleportPos.x + 8, teleportPos.y + 8, teleportPos.z + 8
        );
        List<Entity> nearby = level.getEntities(player, box);
        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity living) {
                float damage = totalDamage;
                // Apply smite bonus vs undead
                if (living.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)) {
                    damage += smiteBonus;
                }
                // Apply bane bonus vs arthropods
                if (living.getType().is(net.minecraft.tags.EntityTypeTags.ARTHROPOD)) {
                    damage += baneBonus;
                }
                living.hurt(level.damageSources().magic(), damage);
            }
        }
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
