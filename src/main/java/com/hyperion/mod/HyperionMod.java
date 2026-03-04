package com.hyperion.mod;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod("hyperion")
public class HyperionMod {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, "hyperion");

    public static final DeferredHolder<Item, HyperionItem> HYPERION_ITEM =
        ITEMS.register("hyperion_sword", HyperionItem::new);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "hyperion");

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HYPERION_TAB =
        CREATIVE_TABS.register("hyperion_tab", () -> CreativeModeTab.builder()
            .title(Component.literal("Hyperion"))
            .icon(() -> HyperionCommand.buildHyperionStack())
            .displayItems((params, output) -> {
                output.accept(HyperionCommand.buildHyperionStack());
            })
            .build()
        );

    public HyperionMod(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        NeoForge.EVENT_BUS.register(HyperionEvents.class);
        NeoForge.EVENT_BUS.register(HyperionCommand.class);
        LOGGER.info("Hyperion mod loaded!");
    }
}
