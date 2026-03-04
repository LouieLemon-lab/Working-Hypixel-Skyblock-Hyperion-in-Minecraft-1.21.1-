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

        Vec3 look = player.getLookAngle();
        Vec3 origin = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 targetRaw = origin.add(look.scale(10));

        // Raycast to find collision point
        BlockHitResult hit = level.clip(new ClipContext(
            origin,
            targetRaw,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            player
        ));

        Vec3 target;
        if (hit.getType() == HitResult.Type.BLOCK) {
            // Stop just before the block
            target = hit.getLocation().subtract(look.scale(0.5));
        } else {
            target = targetRaw;
        }

        // Teleport to safe position (feet level)
        Vec3 teleportPos = new Vec3(target.x, target.y - player.getEyeHeight(), target.z);

        // Wither shield effects
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 4, false, false));

        // Origin particles + sound
        level.sendParticles(
            net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
            origin.x, origin.y, origin.z,
            80, 0.5, 0.8, 0.5, 0.05
        );
        level.playSound(null, origin.x, origin.y, origin.z,
            net.minecraft.sounds.SoundEvents.WITHER_AMBIENT,
            net.minecraft.sounds.SoundSource.MASTER, 1.0f, 1.2f);

        // Teleport player
        player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);

        // Explosion particles + sound at destination
        level.sendParticles(
            net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER,
            teleportPos.x, teleportPos.y + 1, teleportPos.z,
            1, 0, 0, 0, 0
        );
        level.playSound(null, teleportPos.x, teleportPos.y, teleportPos.z,
            net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
            net.minecraft.sounds.SoundSource.MASTER, 2.0f, 0.8f);

        // Damage nearby entities
        AABB box = new AABB(
            teleportPos.x - 8, teleportPos.y - 8, teleportPos.z - 8,
            teleportPos.x + 8, teleportPos.y + 8, teleportPos.z + 8
        );
        List<Entity> nearby = level.getEntities(player, box);
        for (Entity entity : nearby) {
            if (entity instanceof LivingEntity living) {
                living.hurt(level.damageSources().magic(), 1000.0f);
            }
        }

        // Mana message
        player.sendSystemMessage(Component.literal("")
            .append(Component.literal("-250 Mana (")
                .withStyle(s -> s.withColor(0x00AAAA)))
            .append(Component.literal("Wither Impact")
                .withStyle(s -> s.withColor(0xFFAA00)))
            .append(Component.literal(")")
                .withStyle(s -> s.withColor(0x00AAAA)))
        );
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
