package com.thebravestclown.gardenoverflow.block;

import com.thebravestclown.gardenoverflow.block.entity.UnravelingBloomBlockEntity;
import com.thebravestclown.gardenoverflow.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;

public class UnravelingBloomBlock extends FlowerBlock implements EntityBlock {

    public UnravelingBloomBlock() {
        super(MobEffects.REGENERATION, 10, Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.PLANT)
                .noCollission()
                .noOcclusion()
                .instabreak()
                .sound(net.minecraft.world.level.block.SoundType.GRASS)
        );
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new UnravelingBloomBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof UnravelingBloomBlockEntity flower) {
                flower.tickFlower();
            }
        };
    }
}
