package com.thebravestclown.gardenoverflow;

import com.mojang.logging.LogUtils;
import com.thebravestclown.gardenoverflow.config.GardenOverflowConfig;
import com.thebravestclown.gardenoverflow.registry.ModBlockEntities;
import com.thebravestclown.gardenoverflow.registry.ModBlocks;
import com.thebravestclown.gardenoverflow.registry.ModItems;
import com.thebravestclown.gardenoverflow.registry.ModMenus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod("gardenoverflow")
public class GardenOverflowMod {
    public static final String MODID = "gardenoverflow";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GardenOverflowMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register deferred registers
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);

        // Register config
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(
                net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                GardenOverflowConfig.SPEC
        );

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Common setup
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                // Register render type for transparency
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.UNRAVELING_BLOOM.get(),
                    net.minecraft.client.renderer.RenderType.cutout()
                );
                net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.LOGISTICA_FLORIS.get(),
                    net.minecraft.client.renderer.RenderType.cutout()
                );
            });
        }
    }
}
