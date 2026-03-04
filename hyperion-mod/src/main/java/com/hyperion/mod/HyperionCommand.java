package com.hyperion.mod;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class HyperionCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("hyperion")
                .requires(source -> source.hasPermission(0))
                .executes(HyperionCommand::giveHyperion)
        );
    }

    private static int giveHyperion(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            ItemStack stack = new ItemStack(Items.FISHING_ROD);

            // Custom name
            stack.set(DataComponents.CUSTOM_NAME,
                Component.literal("Heroic Hyperion ")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withItalic(false))
                    .append(Component.literal("✪✪✪✪✪")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withItalic(false)))
            );

            // Lore
            List<Component> loreLines = List.of(
                Component.literal("Gear Score: ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                    .append(Component.literal("1092 ").withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withItalic(false)))
                    .append(Component.literal("(3572)").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(false))),
                Component.literal("Damage: ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                    .append(Component.literal("+356").withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withItalic(false)))
                    .append(Component.literal(" (+30) ").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(false)))
                    .append(Component.literal("(+1,299.4)").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(false))),
                Component.literal("Strength: ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                    .append(Component.literal("+230 ").withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withItalic(false)))
                    .append(Component.literal("(+30)").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(false)))
                    .append(Component.literal(" (Heroic +50)").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(false)))
                    .append(Component.literal(" (+839.5)").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(false))),
                Component.literal("Crit Damage: ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                    .append(Component.literal("+60% ").withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withItalic(false)))
                    .append(Component.literal("(+219%)").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(false))),
                Component.literal("Bonus Attack Speed: ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                    .append(Component.literal("+7%").withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withItalic(false)))
                    .append(Component.literal(" (Heroic +7%) ").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(false)))
                    .append(Component.literal("(+9.8%)").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(false))),
                Component.literal(""),
                Component.literal("Ultimate Wise V, ").withStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE).withBold(true).withItalic(false))
                    .append(Component.literal("Critical VI, Cubism V").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withBold(false).withItalic(false))),
                Component.literal("Ender Slayer V, Execute V, Experience III").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(false)),
                Component.literal("Fire Aspect II, First Strike IV, Giant Killer VI").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(false)),
                Component.literal("Impaling III, Lethality VI, Life Steal IV").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(false)),
                Component.literal("Looting IV, Luck VI, Scavenger IV").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(false)),
                Component.literal("Smite VI, Telekinesis I, Thunderlord VI").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(false)),
                Component.literal("Vampirism VI, Venomous V").withStyle(Style.EMPTY.withColor(ChatFormatting.BLUE).withItalic(false)),
                Component.literal(""),
                Component.literal("Deals +").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                    .append(Component.literal("50%").withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withItalic(false)))
                    .append(Component.literal(" damage to Withers.").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))),
                Component.literal("Grants +").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                    .append(Component.literal("1 Damage").withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withItalic(false)))
                    .append(Component.literal(" and ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false)))
                    .append(Component.literal("+2 Intelligence").withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withItalic(false))),
                Component.literal("per Catacombs level.").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false)),
                Component.literal("Your Catacombs Level: ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                    .append(Component.literal("33").withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withItalic(false))),
                Component.literal(""),
                Component.literal("Scroll Abilities:").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false)),
                Component.literal("Item Ability: Wither Impact ").withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withItalic(false))
                    .append(Component.literal("RIGHT CLICK").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true).withItalic(false))),
                Component.literal("Teleport ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                    .append(Component.literal("10 blocks").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN).withItalic(false)))
                    .append(Component.literal(" ahead of you.").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))),
                Component.literal("Then implode dealing ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                    .append(Component.literal("16,133.2").withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withItalic(false)))
                    .append(Component.literal(" damage to nearby enemies.").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))),
                Component.literal("Applies wither shield reducing damage taken").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false)),
                Component.literal("and granting absorption shield for ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))
                    .append(Component.literal("5").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(false)))
                    .append(Component.literal(" seconds.").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false))),
                Component.literal("Mana cost: ").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withItalic(false))
                    .append(Component.literal("250").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA).withItalic(false))),
                Component.literal(""),
                Component.literal("✦ MYTHIC DUNGEON SWORD").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withBold(true).withItalic(false))
            );
            stack.set(DataComponents.LORE, new ItemLore(loreLines));

            // Unbreakable
            stack.set(DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(false));

            // Attribute modifiers
            ItemAttributeModifiers.Builder attrBuilder = ItemAttributeModifiers.builder();
            attrBuilder.add(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath("hyperion", "dmg"),
                    1000.0,
                    AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
            );
            attrBuilder.add(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath("hyperion", "spd"),
                    4.0,
                    AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.MAINHAND
            );
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, attrBuilder.build());

            player.addItem(stack);
            player.sendSystemMessage(Component.literal("§6You received the §dHeroic Hyperion§6!"));
            return 1;
        } catch (Exception e) {
            HyperionMod.LOGGER.error("Failed to give Hyperion", e);
            return 0;
        }
    }
}
