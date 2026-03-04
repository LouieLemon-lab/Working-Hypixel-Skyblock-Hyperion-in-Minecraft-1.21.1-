package com.hyperion.mod;

import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;

public class HyperionRecipe extends CustomRecipe {

    public HyperionRecipe(CraftingBookCategory category) {
        super(category);
    }

    public static boolean isNecronHandle(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.STICK) return false;
        return stack.getHoverName().getString().contains("Necron");
    }

    // Necron's Handle recipe:
    // D * D
    // D X D
    // D * D
    private boolean matchesNecronHandle(CraftingInput c) {
        if (c.width() < 3 || c.height() < 3) return false;
        return c.getItem(0).is(Items.DIAMOND)
            && c.getItem(1).is(Items.NETHER_STAR)
            && c.getItem(2).is(Items.DIAMOND)
            && c.getItem(3).is(Items.DIAMOND)
            && c.getItem(4).is(Items.STICK)
            && c.getItem(5).is(Items.DIAMOND)
            && c.getItem(6).is(Items.DIAMOND)
            && c.getItem(7).is(Items.NETHER_STAR)
            && c.getItem(8).is(Items.DIAMOND);
    }

    // Hyperion recipe:
    // _ E _
    // _ E _
    // _ H _
    private boolean matchesHyperion(CraftingInput c) {
        if (c.width() < 3 || c.height() < 3) return false;
        return c.getItem(0).isEmpty()
            && c.getItem(1).is(Items.ENDER_EYE)
            && c.getItem(2).isEmpty()
            && c.getItem(3).isEmpty()
            && c.getItem(4).is(Items.ENDER_EYE)
            && c.getItem(5).isEmpty()
            && c.getItem(6).isEmpty()
            && isNecronHandle(c.getItem(7))
            && c.getItem(8).isEmpty();
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return matchesNecronHandle(container) || matchesHyperion(container);
    }

    @Override
    public ItemStack assemble(CraftingInput container, net.minecraft.core.RegistryAccess registryAccess) {
        if (matchesNecronHandle(container)) return buildNecronHandle();
        if (matchesHyperion(container)) return HyperionCommand.buildHyperionStack();
        return ItemStack.EMPTY;
    }

    public static ItemStack buildNecronHandle() {
        ItemStack stack = new ItemStack(Items.STICK);
        stack.set(DataComponents.CUSTOM_NAME,
            Component.literal("Necron's Handle")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withItalic(false))
        );
        return stack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HyperionMod.HYPERION_RECIPE_SERIALIZER.get();
    }
}
