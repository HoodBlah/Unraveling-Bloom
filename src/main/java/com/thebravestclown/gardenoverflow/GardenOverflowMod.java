package com.thebravestclown.gardenoverflow;

import com.thebravestclown.gardenoverflow.config.GardenOverflowConfig;
import com.thebravestclown.gardenoverflow.registry.ModRegistry;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import org.slf4j.Logger;

import velrondevs.botania.api.BotaniaRegistries;

@Mod(GardenOverflowMod.MODID)
public class GardenOverflowMod {
    public static final String MODID = "gardenoverflow";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GardenOverflowMod(IEventBus modBus, ModContainer container) {
        ModRegistry.register(modBus);
        container.registerConfig(ModConfig.Type.COMMON, GardenOverflowConfig.SPEC);
        modBus.addListener(GardenOverflowMod::addToTabs);
    }

    /** Botania's tab, plus vanilla Natural Blocks as a fallback. */
    private static void addToTabs(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey().equals(BotaniaRegistries.BOTANIA_TAB_KEY) || e.getTabKey().equals(CreativeModeTabs.NATURAL_BLOCKS)) {
            e.accept(ModRegistry.UNRAVELING_BLOOM_ITEM);
            e.accept(ModRegistry.LOGISTICA_FLORIS_ITEM);
        }
    }
}
