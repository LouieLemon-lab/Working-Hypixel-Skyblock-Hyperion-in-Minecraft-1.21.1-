package com.hyperion.mod;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod("hyperion")
public class HyperionMod {
    public static final Logger LOGGER = LogUtils.getLogger();

    public HyperionMod(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(HyperionEvents.class);
        NeoForge.EVENT_BUS.register(HyperionCommand.class);
        LOGGER.info("Hyperion mod loaded!");
    }
}
