package com.abyanlite.unravelingbloom.block.entity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.abyanlite.unravelingbloom.config.UnravelingBloomConfig;
import com.abyanlite.unravelingbloom.registry.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.RandomSource;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.api.mana.ManaPool;

public class UnravelingBloomBlockEntity extends FunctionalFlowerBlockEntity {

    private static final int MANA_COST = 33333;
    private static final int COOLDOWN = 100;
    private static final int DETECTION_INTERVAL = 20;
    private static final int DETECTION_RADIUS = 2;
    private static final int MANA_SEARCH_RADIUS = 6;

    private int cooldown = 0;
    private int detectionTimer = 0;

    public UnravelingBloomBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UNRAVELING_BLOOM.get(), pos, state);
    }

    @Override
    public void tickFlower() {
        Level level = this.level;
        if (level == null || level.isClientSide) {
            return;
        }

        // Try to pull mana from nearby pools
        if (getMana() < getMaxMana()) {
            for (int x = -MANA_SEARCH_RADIUS; x <= MANA_SEARCH_RADIUS; x++) {
                for (int y = -MANA_SEARCH_RADIUS; y <= MANA_SEARCH_RADIUS; y++) {
                    for (int z = -MANA_SEARCH_RADIUS; z <= MANA_SEARCH_RADIUS; z++) {
                        BlockPos checkPos = getBlockPos().offset(x, y, z);
                        if (level.getBlockEntity(checkPos) instanceof ManaPool pool) {
                            int needed = Math.min(1000, getMaxMana() - getMana());
                            int poolMana = pool.getCurrentMana();
                            if (poolMana > 0) {
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

        // Decrement cooldown
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        // Check for items periodically
        detectionTimer++;
        if (detectionTimer < DETECTION_INTERVAL) {
            return;
        }
        detectionTimer = 0;

        // Check if we have enough mana
        if (getMana() < MANA_COST) {
            return;
        }

        // Search for items in radius
        AABB detectionBox = new AABB(getBlockPos()).inflate(DETECTION_RADIUS);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, detectionBox);

        for (ItemEntity entity : items) {
            ItemStack stack = entity.getItem();

            // Only process single items
            if (stack.getCount() != 1) {
                continue;
            }

            // Validate the item
            if (!isValid(stack)) {
                continue;
            }

            // Attempt uncrafting
            if (attemptUncraft(stack)) {
                // Success: remove the item, subtract mana, set cooldown
                entity.discard();
                addMana(-MANA_COST);
                cooldown = COOLDOWN;
                break;
            }
        }
    }

    /**
     * Validates if an item can be uncrafted.
     * Rejects:
     * - Damaged items
     * - Enchanted items
     * - Items with NBT (except durability)
     * - Items with multiple crafting variants
     * - Items crafted from tags or container items
     */
    private boolean isValid(ItemStack stack) {
        if (stack.isEmpty()) {
            System.out.println("Unraveling Bloom: isValid - stack is empty");
            return false;
        }

        // Reject damaged items (tools/armor that have lost durability)
        if (stack.isDamaged()) {
            System.out.println("Unraveling Bloom: isValid - item is damaged: " + stack.getItem() + " durability: " + stack.getDamageValue() + "/" + stack.getMaxDamage());
            return false;
        }

        // Reject enchanted items
        if (stack.isEnchanted()) {
            return false;
        }

        // Reject items with problematic NBT (custom names, lore, etc.)
        // Allow harmless NBT like durability, colors, etc.
        if (stack.hasTag()) {
            var tag = stack.getTag();
            if (tag != null) {
                // Reject if has custom display name or lore
                if (tag.contains("display")) {
                    var display = tag.getCompound("display");
                    if (display.contains("Name") || display.contains("Lore")) {
                        return false;
                    }
                }
                // Reject if has stored enchantments (like enchanted books)
                if (tag.contains("StoredEnchantments")) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Attempts to uncraft an item by finding its crafting recipe
     * and spawning ingredients with one randomly removed.
     *
     * Returns true if uncrafting was successful.
     */
    private boolean attemptUncraft(ItemStack inputStack) {
        Level level = this.level;
        if (level == null || level.isClientSide) {
            return false;
        }

        // Get the recipe manager
        RecipeManager recipeManager = level.getRecipeManager();

        // Find all crafting recipes that match this item
        // Filter efficiently: check output item and count BEFORE any logging
        List<CraftingRecipe> matchingRecipes = recipeManager.getAllRecipesFor(
                RecipeType.CRAFTING
        )
                .stream()
                .filter(recipe -> {
                    ItemStack recipeOutput = recipe.getResultItem(level.registryAccess());
                    // Quick filter: output must be exactly 1 item and match the input
                    return recipeOutput.getCount() == 1 && ItemStack.isSameItem(inputStack, recipeOutput);
                })
                .collect(Collectors.toList());

        System.out.println("Unraveling Bloom: Found " + matchingRecipes.size() + " recipes for " + inputStack.getItem());

        // Process first matching recipe
        for (CraftingRecipe recipe : matchingRecipes) {
            if (processRecipe(inputStack, recipe)) {
                return true;
            } else {
                System.out.println("Unraveling Bloom: Recipe rejected: " + recipe.getId());
            }
        }

        return false;
    }

    /**
     * Processes a recipe: extracts ingredients, validates them,
     * and spawns ingredients with possible consumption based on recipe size.
     * - Recipes with 4+ ingredients: 50% chance to consume one random ingredient
     * - Recipes with less than 4 ingredients: no consumption, all ingredients returned
     */
    private boolean processRecipe(ItemStack inputStack, CraftingRecipe recipe) {
        Level level = getLevel();
        if (level == null) {
            return false;
        }

        // Extract ingredients
        var ingredients = recipe.getIngredients();

        if (ingredients.isEmpty()) {
            return false;
        }

        // Convert ingredients to concrete ItemStacks
        List<ItemStack> ingredientStacks = new java.util.ArrayList<>();

        for (var ingredient : ingredients) {
            ItemStack[] items = ingredient.getItems();

            // Skip empty ingredients (empty slots in crafting grid)
            if (items.length == 0 || ingredient.isEmpty()) {
                continue;
            }

            // Use first option if multiple (e.g., for tags like #planks)
            ItemStack ingredientStack = items[0].copy();

            // Skip empty stacks
            if (ingredientStack.isEmpty()) {
                continue;
            }

            // Skip container items (milk buckets, water buckets, etc.) - don't return them
            if (!ingredientStack.getCraftingRemainingItem().isEmpty()) {
                System.out.println("Unraveling Bloom: Skipping container item: " + ingredientStack.getItem());
                continue;
            }

            ingredientStacks.add(ingredientStack);
        }

        // Validate: no tags, no container items (already checked above)
        for (ItemStack stack : ingredientStacks) {
            if (stack.hasTag()) {
                System.out.println("Unraveling Bloom: Rejected - ingredient has NBT tag");
                return false;
            }
        }

        // Apply consumption logic based on ingredient count
        boolean consumed = false;
        if (ingredientStacks.size() >= 4) {
            // 50% chance to consume one random ingredient
            if (level.getRandom().nextBoolean()) {
                int removeIndex = level.getRandom().nextInt(ingredientStacks.size());
                ingredientStacks.remove(removeIndex);
                consumed = true;
                // Play burp sound when item is consumed
                level.playSound(null, getBlockPos(), SoundEvents.PLAYER_BURP, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        // If less than 4 ingredients, don't consume anything - all ingredients returned

        // Play anvil land sound for uncrafting (but not if we just burped)
        if (!consumed) {
            level.playSound(null, getBlockPos(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, 1.5F);
        }

        // Spawn remaining ingredients
        for (ItemStack stack : ingredientStacks) {
            ItemEntity itemEntity = new ItemEntity(
                    level,
                    getBlockPos().getX() + 0.5,
                    getBlockPos().getY() + 1.0,
                    getBlockPos().getZ() + 0.5,
                    stack
            );
            itemEntity.setDeltaMovement(
                    (level.getRandom().nextDouble() - 0.5) * 0.2,
                    0.1,
                    (level.getRandom().nextDouble() - 0.5) * 0.2
            );
            level.addFreshEntity(itemEntity);
        }

        return true;
    }

    @Override
    public int getMaxMana() {
        return 200000;
    }

    @Override
    public int getColor() {
        return 0x7FFF7F; // Lime green
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getBlockPos(), DETECTION_RADIUS);
    }
}
