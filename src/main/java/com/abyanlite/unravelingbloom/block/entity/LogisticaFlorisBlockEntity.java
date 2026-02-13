package com.abyanlite.unravelingbloom.block.entity;

import com.abyanlite.unravelingbloom.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.api.corporea.CorporeaSpark;
import vazkii.botania.api.mana.ManaPool;
import net.minecraft.world.entity.player.Player;
import com.abyanlite.unravelingbloom.UnravelingBloomMod;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LogisticaFlorisBlockEntity extends FunctionalFlowerBlockEntity implements Wandable {

    private static final int MANA_COST = 10;              // Mana per item transferred
    private static final int MAX_ITEMS_PER_TRANSFER = 24; // Max items to transfer per cycle
    private static final int TRANSFER_DELAY = 20;         // Ticks between transfers (1 second)
    private static final int SCAN_RADIUS = 1;             // Only check adjacent blocks
    private static final int MANA_SEARCH_RADIUS = 6;      // Range to pull mana from pools
    
    private int transferCooldown = 0;

    public LogisticaFlorisBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LOGISTICA_FLORIS.get(), pos, state);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == BotaniaForgeCapabilities.MANA_RECEIVER) {
            return LazyOptional.of(() -> (T) this).cast();
        }
        if (cap == BotaniaForgeCapabilities.WANDABLE) {
            return LazyOptional.of(() -> (T) this).cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void tickFlower() {
        Level level = this.level;
        if (level == null || level.isClientSide) {
            return;
        }

        // Try to pull mana from nearby pools if we need it
        if (getMana() < getMaxMana()) {
            tryPullMana();
        }

        // Decrement cooldown
        if (transferCooldown > 0) {
            transferCooldown--;
            return;
        }

        // Calculate how many items we can transfer based on available mana
        int currentMana = getMana();
        if (currentMana < MANA_COST) {
            return; // Not enough mana for even one item
        }
        int maxItemsFromMana = Math.min(MAX_ITEMS_PER_TRANSFER, currentMana / MANA_COST);

        // Find a source chest with items
        Container sourceChest = findAdjacentChestWithItems();
        if (sourceChest == null) {
            return; // No chest found, silently wait
        }

        // Extract up to maxItemsFromMana items from source chest
        ItemStack itemsToTransfer = extractItemsFromContainer(sourceChest, maxItemsFromMana);
        if (itemsToTransfer.isEmpty()) {
            return; // Nothing to extract
        }

        // Find Corporea spark on this flower's position
        CorporeaSpark spark = CorporeaHelper.instance().getSparkForBlock(level, getBlockPos());
        if (spark == null) {
            returnItemToContainer(sourceChest, itemsToTransfer);
            return;
        }

        // Find best destination in Corporea network (inventory that already has this item)
        IItemHandler destination = findBestDestinationInNetwork(itemsToTransfer);
        if (destination == null) {
            returnItemToContainer(sourceChest, itemsToTransfer);
            return;
        }

        // Try to insert into destination
        ItemStack remaining = insertIntoHandler(destination, itemsToTransfer);
        
        // Calculate how many items were actually transferred
        int transferred = itemsToTransfer.getCount() - remaining.getCount();
        
        // Return any remaining items to source
        if (!remaining.isEmpty()) {
            returnItemToContainer(sourceChest, remaining);
        }

        // If we transferred anything, consume mana and set cooldown
        if (transferred > 0) {
            addMana(-MANA_COST * transferred);
            transferCooldown = TRANSFER_DELAY;
            setChanged();
            
            // Log successful transfers (reduced spam)
            if (transferred >= 10 || level.getGameTime() % 100 == 0) {
                UnravelingBloomMod.LOGGER.info("Logistica Floris @ {}: Transferred {}x {}. Mana: {}/{}", 
                    getBlockPos(), transferred, itemsToTransfer.getDisplayName().getString(), getMana(), getMaxMana());
            }
        }
    }

    /**
     * Manually pulls mana from nearby mana pools within range
     */
    private void tryPullMana() {
        // Search for mana pools in a 6-block radius
        for (int x = -MANA_SEARCH_RADIUS; x <= MANA_SEARCH_RADIUS; x++) {
            for (int y = -MANA_SEARCH_RADIUS; y <= MANA_SEARCH_RADIUS; y++) {
                for (int z = -MANA_SEARCH_RADIUS; z <= MANA_SEARCH_RADIUS; z++) {
                    BlockPos checkPos = getBlockPos().offset(x, y, z);
                    BlockEntity be = level.getBlockEntity(checkPos);
                    
                    if (be instanceof ManaPool pool) {
                        int poolMana = pool.getCurrentMana();
                        if (poolMana > 0 && getMana() < getMaxMana()) {
                            // Pull more mana per tick to support faster transfers (up to 240 mana needed)
                            int needed = Math.min(250, getMaxMana() - getMana());
                            int toTransfer = Math.min(needed, poolMana);
                            pool.receiveMana(-toTransfer);
                            addMana(toTransfer);
                            
                            if (getMana() >= getMaxMana()) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *  UnravelingBloomMod.LOGGER.info("Logistica Floris @ {}: Successfully transferred 1x {}. Mana remaining: {}", 
            getBlockPos(), itemToTransfer.getDisplayName().getString(), getMana());
    }

    /**
     * Scans all 6 adjacent blocks for containers with items
     */
    private Container findAdjacentChestWithItems() {
        for (Direction dir : Direction.values()) {
            BlockPos checkPos = getBlockPos().relative(dir);
            BlockEntity be = level.getBlockEntity(checkPos);
            
            if (be instanceof Container container) {
                // Check if container has any items
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (!container.getItem(i).isEmpty()) {
                        return container;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Extracts up to maxCount items from the first non-empty slot.
     * Only extracts items of the same type from a single slot.
     */
    private ItemStack extractItemsFromContainer(Container container, int maxCount) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                int extractCount = Math.min(maxCount, stack.getCount());
                ItemStack extracted = stack.split(extractCount);
                container.setChanged();
                return extracted;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Returns an item to a container (undoes extraction if transfer fails)
     */
    private void returnItemToContainer(Container container, ItemStack item) {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack slot = container.getItem(i);
            if (slot.isEmpty()) {
                container.setItem(i, item);
                container.setChanged();
                return;
            } else if (ItemStack.isSameItemSameTags(slot, item) && slot.getCount() < slot.getMaxStackSize()) {
                slot.grow(item.getCount());
                container.setChanged();
                return;
            }
        }
        // If we can't return it, just drop it
        // (shouldn't happen in practice)
    }

    /**
     * Finds the best inventory in the Corporea network for this item type.
     * Prefers inventories that already have this item.
     */
    private IItemHandler findBestDestinationInNetwork(ItemStack item) {
        List<IItemHandler> allInventories = getAllCorporeaInventories();
        
        IItemHandler bestMatch = null;
        int bestMatchCount = 0;
        
        for (IItemHandler handler : allInventories) {
            int existingCount = countMatchingItems(handler, item);
            
            if (existingCount > 0) {
                // This inventory already has the item - prioritize it
                if (existingCount > bestMatchCount) {
                    bestMatch = handler;
                    bestMatchCount = existingCount;
                }
            } else if (bestMatch == null && hasEmptySlot(handler)) {
                // No match yet, but this inventory has space
                bestMatch = handler;
            }
        }
        
        return bestMatch;
    }

    /**
     * Gets all IItemHandler inventories connected to the Corporea network
     */
    private List<IItemHandler> getAllCorporeaInventories() {
        List<IItemHandler> inventories = new ArrayList<>();
        
        // Scan nearby area for Corporea sparks
        int range = 16; // Typical Corporea range
        int sparksFound = 0;
        int inventoriesFound = 0;
        
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos checkPos = getBlockPos().offset(x, y, z);
                    CorporeaSpark spark = CorporeaHelper.instance().getSparkForBlock(level, checkPos);
                    
                    if (spark != null) {
                        sparksFound++;
                        BlockEntity be = level.getBlockEntity(checkPos);
                        if (be != null) {
                            LazyOptional<IItemHandler> cap = be.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
                            if (cap.isPresent()) {
                                inventoriesFound++;
                                cap.ifPresent(inventories::add);
                            }
                        }
                    }
                }
            }
        }
        
        UnravelingBloomMod.LOGGER.info("Logistica Floris @ {}: Scanned network - found {} sparks, {} valid inventories", 
            getBlockPos(), sparksFound, inventoriesFound);
        
        return inventories;
    }

    /**
     * Counts how many of the given item type exist in the handler
     */
    private int countMatchingItems(IItemHandler handler, ItemStack reference) {
        int count = 0;
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack slot = handler.getStackInSlot(i);
            if (ItemStack.isSameItemSameTags(slot, reference)) {
                count += slot.getCount();
            }
        }
        return count;
    }

    /**
     * Checks if the handler has at least one empty slot
     */
    private boolean hasEmptySlot(IItemHandler handler) {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (handler.getStackInSlot(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Attempts to insert an item into a handler
     */
    private ItemStack insertIntoHandler(IItemHandler handler, ItemStack item) {
        ItemStack remaining = item.copy();
        
        // Try to insert into existing stacks first (combine)
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack slot = handler.getStackInSlot(i);
            if (ItemStack.isSameItemSameTags(slot, item) && slot.getCount() < slot.getMaxStackSize()) {
                remaining = handler.insertItem(i, remaining, false);
                if (remaining.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        
        // Try to insert into empty slots
        for (int i = 0; i < handler.getSlots(); i++) {
            if (handler.getStackInSlot(i).isEmpty()) {
                remaining = handler.insertItem(i, remaining, false);
                if (remaining.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        
        return remaining;
    }

    @Override
    public int getMaxMana() {
        return 1000;
    }

    @Override
    public int getColor() {
        return 0xB456D6; // Purple-pink color
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getBlockPos(), SCAN_RADIUS);
    }

    @Override
    public boolean onUsedByWand(@Nullable Player player, ItemStack wand, Direction side) {
        // Allow wand binding (handled by parent) but no special right-click behavior
        return false;
    }
}
