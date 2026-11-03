package com.thebravestclown.gardenoverflow.client;

import com.thebravestclown.gardenoverflow.GardenOverflowMod;
import com.thebravestclown.gardenoverflow.registry.ModRegistry;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import velrondevs.botania.api.BotaniaClientCapabilities;
import velrondevs.botania.api.block_entity.BindableSpecialFlowerBlockEntity.BindableFlowerWandHud;
import velrondevs.botania.client.render.block_entity.SpecialFlowerBlockEntityRenderer;

@EventBusSubscriber(modid = GardenOverflowMod.MODID, value = Dist.CLIENT)
public final class ClientSetup {

    /** Draws the radius overlay while holding a Wand of the Forest. */
    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerBlockEntityRenderer(ModRegistry.UNRAVELING_BLOOM_BE.get(), SpecialFlowerBlockEntityRenderer::new);
        e.registerBlockEntityRenderer(ModRegistry.LOGISTICA_FLORIS_BE.get(), SpecialFlowerBlockEntityRenderer::new);
    }

    /** Mana bar HUD when looking at the flower with a wand. */
    @SubscribeEvent
    public static void wandHuds(RegisterCapabilitiesEvent e) {
        e.registerBlockEntity(BotaniaClientCapabilities.WAND_HUD, ModRegistry.UNRAVELING_BLOOM_BE.get(),
                (be, ctx) -> new BindableFlowerWandHud<>(be));
        e.registerBlockEntity(BotaniaClientCapabilities.WAND_HUD, ModRegistry.LOGISTICA_FLORIS_BE.get(),
                (be, ctx) -> new BindableFlowerWandHud<>(be));
    }

    private ClientSetup() {}
}
