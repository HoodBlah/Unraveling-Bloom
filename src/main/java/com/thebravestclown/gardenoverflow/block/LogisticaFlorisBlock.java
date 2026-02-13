package com.thebravestclown.gardenoverflow.block;

import com.thebravestclown.gardenoverflow.block.entity.LogisticaFlorisBlockEntity;
import com.thebravestclown.gardenoverflow.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.Level;

public class LogisticaFlorisBlock extends FlowerBlock implements EntityBlock {
    // TESTING: FACING property commented out to debug wand binding issue
    // public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public LogisticaFlorisBlock() {
        super(MobEffects.REGENERATION, 10, Properties.of()
                .mapColor(net.minecraft.world.level.material.MapColor.PLANT)
                .noCollission()
                .noOcclusion()
                .instabreak()
                .sound(net.minecraft.world.level.block.SoundType.GRASS)
        );
        // TESTING: Removed FACING initialization
        // this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    // TESTING: Commented out to remove FACING property
    // @Override
    // protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    //     builder.add(FACING);
    // }

    // TESTING: Commented out to remove FACING property
    // @Override
    // public BlockState getStateForPlacement(BlockPlaceContext context) {
    //     return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    // }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LogisticaFlorisBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof LogisticaFlorisBlockEntity flower) {
                flower.tickFlower();
            }
        };
    }
}
