package com.abyanlite.unravelingbloom.event;

import com.abyanlite.unravelingbloom.UnravelingBloomMod;
import com.abyanlite.unravelingbloom.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = UnravelingBloomMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {
    
    @SubscribeEvent
    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Add to Botania's Functional Flowers tab if available
        if (event.getTabKey().location().toString().equals("botania:functional_flowers")) {
            event.accept(ModItems.UNRAVELING_BLOOM);
        }
        
        // Also add to Nature Blocks tab as fallback
        if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
            event.accept(ModItems.UNRAVELING_BLOOM);
        }
    }
}
