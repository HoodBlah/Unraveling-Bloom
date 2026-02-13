package com.thebravestclown.gardenoverflow.event;

import com.thebravestclown.gardenoverflow.GardenOverflowMod;
import com.thebravestclown.gardenoverflow.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GardenOverflowMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    
    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Add to Botania's Functional Flowers tab if available
        if (event.getTabKey().location().toString().equals("botania:functional_flowers")) {
            event.accept(ModItems.UNRAVELING_BLOOM);
            event.accept(ModItems.LOGISTICA_FLORIS);
        }
        
        // Also add to Nature Blocks tab as fallback
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModItems.UNRAVELING_BLOOM);
            event.accept(ModItems.LOGISTICA_FLORIS);
        }
    }
}
