package com.abyanlite.unravelingbloom;

import com.abyanlite.unravelingbloom.config.UnravelingBloomConfig;
import com.abyanlite.unravelingbloom.registry.ModBlockEntities;
import com.abyanlite.unravelingbloom.registry.ModBlocks;
import com.abyanlite.unravelingbloom.registry.ModItems;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("unravelingbloom")
public class UnravelingBloomMod {
    public static final String MODID = "unravelingbloom";

    public UnravelingBloomMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register deferred registers
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModItems.register(modEventBus);

        // Register config
        net.minecraftforge.fml.ModLoadingContext.get().registerConfig(
                net.minecraftforge.fml.config.ModConfig.Type.COMMON,
                UnravelingBloomConfig.SPEC
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
            });
        }
    }
}
