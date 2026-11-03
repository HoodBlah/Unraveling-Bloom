package com.thebravestclown.gardenoverflow.registry;

import com.thebravestclown.gardenoverflow.GardenOverflowMod;
import com.thebravestclown.gardenoverflow.block.entity.LogisticaFlorisBlockEntity;
import com.thebravestclown.gardenoverflow.block.entity.UnravelingBloomBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import velrondevs.botania.common.block.SpecialFlowerBlock;
import velrondevs.botania.common.item.block.SpecialFlowerBlockItem;

public final class ModRegistry {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(GardenOverflowMod.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(GardenOverflowMod.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, GardenOverflowMod.MODID);

    // Botania's own SpecialFlowerBlock handles ticking, shape, floating-flower support, etc.
    public static final DeferredBlock<Block> UNRAVELING_BLOOM = BLOCKS.register("unraveling_bloom",
            () -> new SpecialFlowerBlock(MobEffects.REGENERATION, 10, flowerProps(),
                    () -> ModRegistry.UNRAVELING_BLOOM_BE.get()));
    public static final DeferredBlock<Block> LOGISTICA_FLORIS = BLOCKS.register("logistica_floris",
            () -> new SpecialFlowerBlock(MobEffects.REGENERATION, 10, flowerProps(),
                    () -> ModRegistry.LOGISTICA_FLORIS_BE.get()));

    public static final DeferredItem<Item> UNRAVELING_BLOOM_ITEM = ITEMS.register("unraveling_bloom",
            () -> new SpecialFlowerBlockItem(UNRAVELING_BLOOM.get(), new Item.Properties()));
    public static final DeferredItem<Item> LOGISTICA_FLORIS_ITEM = ITEMS.register("logistica_floris",
            () -> new SpecialFlowerBlockItem(LOGISTICA_FLORIS.get(), new Item.Properties()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<UnravelingBloomBlockEntity>> UNRAVELING_BLOOM_BE =
            BLOCK_ENTITIES.register("unraveling_bloom", () ->
                    BlockEntityType.Builder.of(UnravelingBloomBlockEntity::new, UNRAVELING_BLOOM.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LogisticaFlorisBlockEntity>> LOGISTICA_FLORIS_BE =
            BLOCK_ENTITIES.register("logistica_floris", () ->
                    BlockEntityType.Builder.of(LogisticaFlorisBlockEntity::new, LOGISTICA_FLORIS.get()).build(null));

    private static BlockBehaviour.Properties flowerProps() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY);
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }

    private ModRegistry() {}
}
