package com.hyperion.mod;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
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
        RECIPE_SERIALIZERS.register("hyperion_craft", HyperionRecipeSerializer::new);

    public HyperionMod(IEventBus modEventBus) {
        RECIPE_SERIALIZERS.register(modEventBus);
        NeoForge.EVENT_BUS.register(HyperionEvents.class);
        NeoForge.EVENT_BUS.register(HyperionCommand.class);
        LOGGER.info("Hyperion mod loaded!");
    }
}
