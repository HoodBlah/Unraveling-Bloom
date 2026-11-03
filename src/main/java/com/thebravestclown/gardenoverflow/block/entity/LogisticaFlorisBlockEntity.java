package com.thebravestclown.gardenoverflow.block.entity;

import com.thebravestclown.gardenoverflow.config.GardenOverflowConfig;
import com.thebravestclown.gardenoverflow.registry.ModRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import velrondevs.botania.api.block_entity.FunctionalFlowerBlockEntity;
import velrondevs.botania.api.block_entity.RadiusDescriptor;
import velrondevs.botania.api.corporea.CorporeaHelper;
import velrondevs.botania.api.corporea.CorporeaNode;
import velrondevs.botania.api.corporea.CorporeaSpark;

/**
 * Moves items from an adjacent container into the best inventory on the Corporea network
 * of the spark attached to this flower. Costs and speed are set in the config.
 */
public class LogisticaFlorisBlockEntity extends FunctionalFlowerBlockEntity {
    private int cooldown = 0;

    public LogisticaFlorisBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.LOGISTICA_FLORIS_BE.get(), pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower(); // pulls mana from the bound pool, handles client particles
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }
        if (getBindingPos() == null && level.getGameTime() % 40 == 0) {
            BlockPos target = findClosestTarget();
            if (target != null) {
                setBindingPos(target);
            }
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        int manaPerItem = GardenOverflowConfig.LOGISTICA_MANA_PER_ITEM.get();
        int maxItems = Math.min(GardenOverflowConfig.LOGISTICA_MAX_ITEMS.get(), getMana() / manaPerItem);
        if (maxItems <= 0) {
            return;
        }
        CorporeaSpark spark = CorporeaHelper.instance().getSparkForBlock(level, getBlockPos());
        if (spark == null) {
            return;
        }

        // Find the first non-empty slot of an adjacent container
        for (Direction dir : Direction.values()) {
            BlockPos sourcePos = getBlockPos().relative(dir);
            if (!(level.getBlockEntity(sourcePos) instanceof Container source)) {
                continue;
            }
            for (int slot = 0; slot < source.getContainerSize(); slot++) {
                if (!source.getItem(slot).isEmpty()) {
                    transfer(level, spark, source, slot, sourcePos, maxItems);
                    return;
                }
            }
        }
    }

    private void transfer(Level level, CorporeaSpark spark, Container source, int slot, BlockPos sourcePos, int maxItems) {
        ItemStack taken = source.getItem(slot).split(maxItems);
        int total = taken.getCount();
        source.setChanged();

        IItemHandler destination = findDestination(level, spark, taken, sourcePos);
        ItemStack remaining = destination == null ? taken : ItemHandlerHelper.insertItemStacked(destination, taken, false);

        int moved = total - remaining.getCount();
        if (!remaining.isEmpty()) { // put back whatever did not fit
            ItemStack current = source.getItem(slot);
            if (current.isEmpty()) {
                source.setItem(slot, remaining);
            } else {
                current.grow(remaining.getCount());
            }
            source.setChanged();
        }
        if (moved > 0) {
            addMana(-GardenOverflowConfig.LOGISTICA_MANA_PER_ITEM.get() * moved);
            cooldown = GardenOverflowConfig.LOGISTICA_DELAY_TICKS.get();
        }
    }

    /** Prefers the inventory already holding the most of this item, else the first with a free slot. */
    private IItemHandler findDestination(Level level, CorporeaSpark spark, ItemStack item, BlockPos sourcePos) {
        IItemHandler best = null;
        int bestCount = 0;
        for (CorporeaNode node : CorporeaHelper.instance().getNodesOnNetwork(spark)) {
            BlockPos pos = node.getPos();
            if (node.getWorld() != level || pos.equals(sourcePos) || pos.equals(getBlockPos())) {
                continue;
            }
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler == null) {
                continue;
            }
            int count = 0;
            boolean hasFreeSlot = false;
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack s = handler.getStackInSlot(i);
                if (s.isEmpty()) {
                    hasFreeSlot = true;
                } else if (ItemStack.isSameItemSameComponents(s, item)) {
                    count += s.getCount();
                }
            }
            if (count > bestCount) {
                best = handler;
                bestCount = count;
            } else if (best == null && hasFreeSlot) {
                best = handler;
            }
        }
        return best;
    }

    @Override
    public int getMaxMana() {
        return GardenOverflowConfig.LOGISTICA_MANA_CAPACITY.get();
    }

    @Override
    public int getColor() {
        return 0xB456D6;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), 1);
    }
}
