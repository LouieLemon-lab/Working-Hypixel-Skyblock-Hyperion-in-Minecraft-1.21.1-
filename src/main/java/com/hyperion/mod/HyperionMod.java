package com.hyperion.mod;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod("hyperion")
public class HyperionMod {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, "hyperion");

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<HyperionRecipe>> HYPERION_RECIPE_SERIALIZER =
        RECIPE_SERIALIZERS.register("hyperion_craft", () -> new RecipeSerializer<>() {
            @Override
            public MapCodec<HyperionRecipe> codec() {
                return MapCodec.unit(new HyperionRecipe(CraftingBookCategory.MISC));
            }

            @Override
            public StreamCodec<RegistryFriendlyByteBuf, HyperionRecipe> streamCodec() {
                return StreamCodec.of(
                    (buf, r) -> {},
                    buf -> new HyperionRecipe(CraftingBookCategory.MISC)
                );
            }
        });

    public HyperionMod(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
        NeoForge.EVENT_BUS.register(HyperionEvents.class);
        NeoForge.EVENT_BUS.register(HyperionCommand.class);
        LOGGER.info("Hyperion mod loaded!");
    }
}
