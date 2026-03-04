package com.hyperion.mod;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class HyperionRecipeSerializer implements RecipeSerializer<HyperionRecipe> {

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
}
