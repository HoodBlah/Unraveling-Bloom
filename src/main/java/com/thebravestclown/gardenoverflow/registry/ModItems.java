package com.thebravestclown.gardenoverflow.registry;

import com.thebravestclown.gardenoverflow.GardenOverflowMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(
            ForgeRegistries.ITEMS,
            GardenOverflowMod.MODID
    );

    public static final RegistryObject<Item> UNRAVELING_BLOOM = ITEMS.register("unraveling_bloom",
            () -> new BlockItem(ModBlocks.UNRAVELING_BLOOM.get(), new Item.Properties()));

    public static final RegistryObject<Item> LOGISTICA_FLORIS = ITEMS.register("logistica_floris",
            () -> new BlockItem(ModBlocks.LOGISTICA_FLORIS.get(), new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
