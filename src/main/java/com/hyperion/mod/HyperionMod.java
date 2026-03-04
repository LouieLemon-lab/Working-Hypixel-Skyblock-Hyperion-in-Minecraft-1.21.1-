package com.hyperion.mod;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slf4j.Logger;

@Mod("hyperion")
public class HyperionMod {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, "hyperion");

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<HyperionRecipe>> HYPERION_RECIPE_SERIALIZER =
        RECIPE_SERIALIZERS.register("hyperion_craft", () -> new RecipeSerializer<HyperionRecipe>() {
            @Override
            public HyperionRecipe fromJson(net.minecraft.resources.ResourceLocation id, com.google.gson.JsonObject json) {
                return new HyperionRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
            }
            @Override
            public HyperionRecipe fromNetwork(net.minecraft.resources.ResourceLocation id, net.minecraft.network.FriendlyByteBuf buf) {
                return new HyperionRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
            }
            @Override
            public void toNetwork(net.minecraft.network.FriendlyByteBuf buf, HyperionRecipe recipe) {}

            @Override
            public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, HyperionRecipe> streamCodec() {
                return net.minecraft.network.codec.StreamCodec.of(
                    (buf, r) -> {},
                    buf -> new HyperionRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC)
                );
            }
            @Override
            public com.mojang.serialization.MapCodec<HyperionRecipe> codec() {
                return com.mojang.serialization.MapCodec.unit(
                    new HyperionRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC)
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
