package com.hyperion.mod;

import net.minecraft.world.inventory.CraftingContainer;
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
    private boolean matchesNecronHandle(CraftingContainer c) {
        if (c.getWidth() < 3 || c.getHeight() < 3) return false;
        ItemStack[][] g = getGrid(c);
        return g[0][0].is(Items.DIAMOND)
            && g[0][1].is(Items.NETHER_STAR)
            && g[0][2].is(Items.DIAMOND)
            && g[1][0].is(Items.DIAMOND)
            && g[1][1].is(Items.STICK)
            && g[1][2].is(Items.DIAMOND)
            && g[2][0].is(Items.DIAMOND)
            && g[2][1].is(Items.NETHER_STAR)
            && g[2][2].is(Items.DIAMOND);
    }

    // Hyperion recipe:
    // _ E _
    // _ E _
    // _ H _
    private boolean matchesHyperion(CraftingContainer c) {
        if (c.getWidth() < 3 || c.getHeight() < 3) return false;
        ItemStack[][] g = getGrid(c);
        for (int row = 0; row < 3; row++) {
            if (!g[row][0].isEmpty()) return false;
            if (!g[row][2].isEmpty()) return false;
        }
        return g[0][1].is(Items.ENDER_EYE)
            && g[1][1].is(Items.ENDER_EYE)
            && isNecronHandle(g[2][1]);
    }

    private ItemStack[][] getGrid(CraftingContainer c) {
        ItemStack[][] grid = new ItemStack[3][3];
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                grid[row][col] = c.getItem(row * 3 + col);
        return grid;
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        return matchesNecronHandle(container) || matchesHyperion(container);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, net.minecraft.core.RegistryAccess registryAccess) {
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
