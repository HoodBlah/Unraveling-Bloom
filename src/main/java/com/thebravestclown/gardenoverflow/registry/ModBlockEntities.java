package com.thebravestclown.gardenoverflow.registry;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import com.thebravestclown.gardenoverflow.GardenOverflowMod;
import com.thebravestclown.gardenoverflow.block.entity.UnravelingBloomBlockEntity;
import com.thebravestclown.gardenoverflow.block.entity.LogisticaFlorisBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import vazkii.botania.api.block.Bound;
import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(
            ForgeRegistries.BLOCK_ENTITY_TYPES,
            GardenOverflowMod.MODID
    );

    @SuppressWarnings("unchecked")
    public static final Supplier<BlockEntityType<?>> UNRAVELING_BLOOM =
            BLOCK_ENTITIES.register("unraveling_bloom", () ->
                    BlockEntityType.Builder.of(
                            UnravelingBloomBlockEntity::new,
                            ModBlocks.UNRAVELING_BLOOM.get()
                    ).build(null)
            );

    @SuppressWarnings("unchecked")
    public static final Supplier<BlockEntityType<?>> LOGISTICA_FLORIS =
            BLOCK_ENTITIES.register("logistica_floris", () ->
                    BlockEntityType.Builder.of(
                            LogisticaFlorisBlockEntity::new,
                            ModBlocks.LOGISTICA_FLORIS.get()
                    ).build(null)
            );

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
