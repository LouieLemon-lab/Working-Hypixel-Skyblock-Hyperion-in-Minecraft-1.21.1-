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

        Vec3 look = player.getLookAngle();
        Vec3 playerPos = player.position();

        Vec3 flatLook = new Vec3(look.x, 0, look.z).normalize();
        Vec3 eyePos = playerPos.add(0, player.getEyeHeight(), 0);
        Vec3 eyeTarget = eyePos.add(flatLook.scale(10));

        BlockHitResult hit = level.clip(new ClipContext(
            eyePos, eyeTarget,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            player
        ));

        double destX, destZ;
        if (hit.getType() == HitResult.Type.BLOCK) {
            Vec3 hitPos = hit.getLocation().subtract(flatLook.scale(0.6));
            destX = hitPos.x;
            destZ = hitPos.z;
        } else {
            destX = playerPos.x + flatLook.x * 10;
            destZ = playerPos.z + flatLook.z * 10;
        }

        double destY = findGroundY(level, destX, playerPos.y, destZ);
        Vec3 teleportFeet = new Vec3(destX, destY, destZ);
        Vec3 originPos = playerPos.add(0, 1, 0);

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

    private static double findGroundY(ServerLevel level, double x, double startY, double z) {
        for (int i = 0; i <= 20; i++) {
            BlockPos floor = BlockPos.containing(x, startY - i, z);
            BlockPos feet = floor.above();
            BlockPos head = floor.above(2);
            if (!level.getBlockState(floor).isAir() && isClear(level, feet) && isClear(level, head)) {
                return feet.getY();
            }
        }
        for (int i = 1; i <= 20; i++) {
            BlockPos floor = BlockPos.containing(x, startY + i, z);
            BlockPos feet = floor.above();
            BlockPos head = floor.above(2);
            if (!level.getBlockState(floor).isAir() && isClear(level, feet) && isClear(level, head)) {
                return feet.getY();
            }
        }
        return startY;
    }

    private static boolean isClear(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.isAir()
            || state.getFluidState().is(FluidTags.WATER)
            || state.getFluidState().is(FluidTags.LAVA);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof WitherBoss)) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!isHyperion(player.getMainHandItem())) return;
        event.setAmount(event.getAmount() * 1.5f);
    }
}
