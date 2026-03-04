package com.hyperion.mod;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.FluidTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class HyperionEvents {

    public static boolean isHyperion(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.IRON_SWORD) return false;
        return stack.getHoverName().getString().contains("Hyperion");
    }

    // When a plain iron sword is crafted, swap it for the real Hyperion
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        if (result.isEmpty() || result.getItem() != Items.IRON_SWORD) return;
        if (result.has(DataComponents.CUSTOM_NAME)) return; // already a Hyperion
        result.applyComponents(HyperionCommand.buildHyperionStack().getComponents());
    }

    public static void doWitherImpact(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ItemStack sword = player.getMainHandItem();

        Vec3 look = player.getLookAngle();
        Vec3 eyePos = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 targetRaw = eyePos.add(look.scale(10));

        BlockHitResult hit = level.clip(new ClipContext(
            eyePos, targetRaw,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            player
        ));

        Vec3 teleportFeet = findSafeTeleportPos(level, player, look, hit, targetRaw);
        Vec3 originPos = player.position().add(0, 1, 0);

        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4, false, false));

        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
            originPos.x, originPos.y, originPos.z, 80, 0.5, 0.8, 0.5, 0.05);

        player.teleportTo(teleportFeet.x, teleportFeet.y, teleportFeet.z);

        level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
            teleportFeet.x, teleportFeet.y + 1, teleportFeet.z, 1, 0, 0, 0, 0);
        level.playSound(null, teleportFeet.x, teleportFeet.y, teleportFeet.z,
            net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
            net.minecraft.sounds.SoundSource.MASTER, 2.0f, 0.8f);

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
        float totalDamage = baseDamage + sharpness * baseDamage * 0.05f;
        float smiteBonus = smite * baseDamage * 0.25f;
        float baneBonus = bane * baseDamage * 0.25f;

        AABB box = new AABB(teleportFeet.x - 8, teleportFeet.y - 8, teleportFeet.z - 8,
                            teleportFeet.x + 8, teleportFeet.y + 8, teleportFeet.z + 8);
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

    private static Vec3 findSafeTeleportPos(ServerLevel level, ServerPlayer player, Vec3 look, BlockHitResult hit, Vec3 targetRaw) {
        double eyeHeight = player.getEyeHeight();
        Vec3 startPos = (hit.getType() == HitResult.Type.BLOCK) ? hit.getLocation() : targetRaw;

        for (double stepBack = 0.5; stepBack <= 10.0; stepBack += 0.5) {
            Vec3 candidate = startPos.subtract(look.scale(stepBack));
            Vec3 candidateFeet = new Vec3(candidate.x, candidate.y - eyeHeight, candidate.z);
            candidateFeet = pushAboveGround(level, candidateFeet);

            BlockPos feet = BlockPos.containing(candidateFeet.x, candidateFeet.y, candidateFeet.z);
            BlockPos head = BlockPos.containing(candidateFeet.x, candidateFeet.y + 1.8, candidateFeet.z);

            if (isClear(level, feet) && isClear(level, head)) return candidateFeet;
        }
        return player.position();
    }

    private static Vec3 pushAboveGround(ServerLevel level, Vec3 pos) {
        for (int i = 0; i < 4; i++) {
            BlockPos bp = BlockPos.containing(pos.x, pos.y + i, pos.z);
            if (isClear(level, bp) && isClear(level, bp.above())) {
                return new Vec3(pos.x, pos.y + i, pos.z);
            }
        }
        return pos;
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
