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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    /** Pause after a pass in which nothing could be moved, so a blocked chest is not rescanned every tick. */
    private static final int RETRY_DELAY_TICKS = 10;

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

        Set<CorporeaNode> nodes = CorporeaHelper.instance().getNodesOnNetwork(spark);

        // Try every occupied slot of every adjacent container, so an item the network cannot take
        // does not block the rest of the chest.
        for (Direction dir : Direction.values()) {
            BlockPos sourcePos = getBlockPos().relative(dir);
            if (!(level.getBlockEntity(sourcePos) instanceof Container source)) {
                continue;
            }
            for (int slot = 0; slot < source.getContainerSize(); slot++) {
                if (source.getItem(slot).isEmpty()) {
                    continue;
                }
                if (transfer(level, nodes, source, slot, sourcePos, maxItems)) {
                    cooldown = GardenOverflowConfig.LOGISTICA_DELAY_TICKS.get();
                    return;
                }
            }
        }
        // Nothing anywhere could be moved: back off for a moment instead of rescanning every tick.
        cooldown = RETRY_DELAY_TICKS;
    }

    private boolean transfer(Level level, Set<CorporeaNode> nodes, Container source, int slot, BlockPos sourcePos, int maxItems) {
        ItemStack inSlot = source.getItem(slot);
        if (inSlot.isEmpty()) {
            return false;
        }
        List<IItemHandler> destinations = findDestinations(level, nodes, inSlot, sourcePos);
        if (destinations.isEmpty()) {
            return false;
        }

        ItemStack taken = inSlot.split(maxItems);
        int total = taken.getCount();
        source.setChanged();

        // Fill the preferred inventory first, then spill whatever is left over into the next best ones.
        ItemStack remaining = taken;
        for (IItemHandler destination : destinations) {
            remaining = ItemHandlerHelper.insertItemStacked(destination, remaining, false);
            if (remaining.isEmpty()) {
                break;
            }
        }

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
        }
        return moved > 0;
    }

    /**
     * Candidate inventories on the network, the one already holding the most of this item first, so a
     * full preferred inventory falls through to the next candidate instead of blocking the transfer.
     */
    private List<IItemHandler> findDestinations(Level level, Set<CorporeaNode> nodes, ItemStack item, BlockPos sourcePos) {
        List<Destination> candidates = new ArrayList<>();
        for (CorporeaNode node : nodes) {
            BlockPos pos = node.getPos();
            if (node.getWorld() != level || pos.equals(sourcePos) || pos.equals(getBlockPos())) {
                continue;
            }
            IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
            if (handler == null) {
                continue;
            }
            int count = 0;
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack s = handler.getStackInSlot(i);
                if (ItemStack.isSameItemSameComponents(s, item)) {
                    count += s.getCount();
                }
            }
            candidates.add(new Destination(handler, count));
        }
        // Stable sort: inventories holding the same amount keep their network order.
        candidates.sort((a, b) -> Integer.compare(b.count(), a.count()));

        List<IItemHandler> result = new ArrayList<>(candidates.size());
        for (Destination candidate : candidates) {
            result.add(candidate.handler());
        }
        return result;
    }

    private record Destination(IItemHandler handler, int count) {}

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
