package com.thebravestclown.gardenoverflow;

import com.thebravestclown.gardenoverflow.config.GardenOverflowConfig;
import com.thebravestclown.gardenoverflow.registry.ModBlockEntities;
import com.thebravestclown.gardenoverflow.registry.ModBlocks;
import com.thebravestclown.gardenoverflow.registry.ModItems;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("gardenoverflow")
public class GardenOverflowMod {
    public static final String MODID = "gardenoverflow";

    public GardenOverflowMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register deferred registers
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);

        // Register config
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(
                net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                GardenOverflowConfig.SPEC
        );

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Common setup tasks here
        });
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD)
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
