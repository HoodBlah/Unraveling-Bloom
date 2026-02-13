package com.thebravestclown.gardenoverflow.registry;

import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.thebravestclown.gardenoverflow.GardenOverflowMod;
import com.thebravestclown.gardenoverflow.block.UnravelingBloomBlock;
import com.thebravestclown.gardenoverflow.block.LogisticaFlorisBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
            ForgeRegistries.BLOCKS,
            GardenOverflowMod.MODID
    );

    public static final RegistryObject<Block> UNRAVELING_BLOOM = BLOCKS.register("unraveling_bloom",
            UnravelingBloomBlock::new);

    public static final RegistryObject<Block> LOGISTICA_FLORIS = BLOCKS.register("logistica_floris",
            LogisticaFlorisBlock::new);

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
