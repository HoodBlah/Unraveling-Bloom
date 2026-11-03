package com.thebravestclown.gardenoverflow.block.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import com.thebravestclown.gardenoverflow.GardenOverflowMod;
import com.thebravestclown.gardenoverflow.config.GardenOverflowConfig;
import com.thebravestclown.gardenoverflow.registry.ModRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import velrondevs.botania.api.block_entity.FunctionalFlowerBlockEntity;
import velrondevs.botania.api.block_entity.RadiusDescriptor;

/**
 * Throw a crafted item near the flower and it is broken back into its recipe ingredients.
 * Mana is drawn from the bound pool by Botania's FunctionalFlowerBlockEntity.
 */
public class UnravelingBloomBlockEntity extends FunctionalFlowerBlockEntity {
    private static final int DETECTION_INTERVAL = 20;

    private int cooldown = 0;
    private int detectionTimer = 0;

    public UnravelingBloomBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistry.UNRAVELING_BLOOM_BE.get(), pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower(); // pulls mana from the bound pool, handles client particles
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }

        // A pool placed after the flower is picked up automatically (Botania only binds on placement)
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
        if (++detectionTimer < DETECTION_INTERVAL) {
            return;
        }
        detectionTimer = 0;

        AABB box = new AABB(getEffectivePos()).inflate(GardenOverflowConfig.DETECTION_RADIUS.get());
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box);
        if (items.isEmpty()) {
            return;
        }

        int cost = GardenOverflowConfig.MANA_COST.get();
        if (getMana() < cost) {
            debug("{} item(s) nearby but not enough mana ({}/{}); bound pool: {}", items.size(), getMana(), cost, getBindingPos());
            return;
        }

        for (ItemEntity entity : items) {
            ItemStack stack = entity.getItem();
            String rejection = rejectionReason(stack);
            if (rejection != null) {
                debug("skipping {}: {}", stack, rejection);
                continue;
            }
            Uncraft uncraft = findUncraft(level, stack);
            if (uncraft == null) {
                continue;
            }

            ItemStack remaining = stack.copy();
            remaining.shrink(uncraft.consumed);
            if (remaining.isEmpty()) {
                entity.discard();
            } else {
                entity.setItem(remaining);
            }
            spawnResults(level, uncraft.results);
            addMana(-cost);
            cooldown = GardenOverflowConfig.COOLDOWN_TICKS.get();
            return;
        }
    }

    private static final List<DataComponentType<?>> CUSTOM_DATA_TYPES = List.of(
            DataComponents.CUSTOM_NAME, DataComponents.LORE,
            DataComponents.ENCHANTMENTS, DataComponents.STORED_ENCHANTMENTS);

    /**
     * Returns why a stack can't be uncrafted, or null if it's fine. Only data that differs from the item's
     * own defaults counts, so items with default components added by other mods are not rejected.
     */
    private static String rejectionReason(ItemStack stack) {
        if (stack.isEmpty()) {
            return "empty";
        }
        if (stack.isDamaged()) {
            return "damaged";
        }
        for (DataComponentType<?> type : CUSTOM_DATA_TYPES) {
            Object value = stack.get(type);
            if (value == null || (value instanceof ItemEnchantments enchants && enchants.isEmpty())) {
                continue;
            }
            if (!Objects.equals(value, stack.getItem().components().get(type))) {
                return "has custom " + BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type) + " = " + value;
            }
        }
        return null;
    }

    private record Uncraft(List<ItemStack> results, int consumed) {}

    /**
     * Finds the crafting recipes that produce this item and uncrafts via one of them, chosen at random.
     * Recipes that take the finished item itself as an ingredient (NBT-reset crafts) are ignored.
     * If the chosen recipe can't be uncrafted, the remaining ones are tried in random order.
     */
    private Uncraft findUncraft(Level level, ItemStack input) {
        List<RecipeHolder<CraftingRecipe>> matches = new ArrayList<>(level.getRecipeManager()
                .getAllRecipesFor(RecipeType.CRAFTING).stream()
                .filter(holder -> {
                    ItemStack result = holder.value().getResultItem(level.registryAccess());
                    return ItemStack.isSameItem(input, result)
                            && input.getCount() >= result.getCount()
                            && holder.value().getIngredients().stream()
                                    .noneMatch(ingredient -> !ingredient.isEmpty() && ingredient.test(result));
                })
                .toList());

        if (matches.isEmpty()) {
            debug("no usable crafting recipe (or stack too small for it) for {}", input);
            return null;
        }
        Collections.shuffle(matches, new Random(level.getRandom().nextLong()));
        debug("{} candidate recipe(s) for {}: {}", matches.size(), input, matches.stream().map(RecipeHolder::id).toList());

        for (RecipeHolder<CraftingRecipe> holder : matches) {
            List<ItemStack> results = rollIngredients(level, holder.value());
            if (results != null) {
                int consumed = holder.value().getResultItem(level.registryAccess()).getCount();
                debug("uncrafting {} via {} into {}", input, holder.id(), results);
                return new Uncraft(results, consumed);
            }
        }
        debug("recipes found for {} but none could be uncrafted", input);
        return null;
    }

    /**
     * Picks one concrete item per ingredient (random for tags), skips container items, and, if the recipe has
     * enough ingredients, may lose one at random. Returns null if the recipe can't be uncrafted.
     */
    private List<ItemStack> rollIngredients(Level level, CraftingRecipe recipe) {
        List<ItemStack> results = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (ingredient.isEmpty()) {
                continue;
            }
            ItemStack[] options = ingredient.getItems();
            if (options.length == 0) {
                continue;
            }
            ItemStack pick = options[level.getRandom().nextInt(options.length)].copy();
            if (pick.isEmpty() || !pick.getCraftingRemainingItem().isEmpty()) {
                continue; // buckets, bottles, etc. are not returned
            }
            if (!pick.getComponentsPatch().isEmpty()) {
                return null; // ingredient needs special data, can't be reproduced safely
            }
            pick.setCount(1);
            results.add(pick);
        }
        if (results.isEmpty()) {
            return null;
        }

        if (results.size() >= GardenOverflowConfig.LOSS_MIN_INGREDIENTS.get()
                && level.getRandom().nextInt(100) < GardenOverflowConfig.CONSUME_CHANCE_PERCENT.get()) {
            ItemStack lost = results.remove(level.getRandom().nextInt(results.size()));
            debug("lost ingredient: {}", lost);
            level.playSound(null, getEffectivePos(), SoundEvents.PLAYER_BURP, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            level.playSound(null, getEffectivePos(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.3F, 1.5F);
        }
        return results;
    }

    private void spawnResults(Level level, List<ItemStack> results) {
        BlockPos pos = getEffectivePos();
        for (ItemStack stack : results) {
            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, stack);
            drop.setDeltaMovement((level.getRandom().nextDouble() - 0.5) * 0.2, 0.1,
                    (level.getRandom().nextDouble() - 0.5) * 0.2);
            level.addFreshEntity(drop);
        }
    }

    private static void debug(String message, Object... args) {
        if (GardenOverflowConfig.DEBUG_LOGGING.get()) {
            GardenOverflowMod.LOGGER.info("[Unraveling Bloom] " + message, args);
        }
    }

    @Override
    public int getMaxMana() {
        return GardenOverflowConfig.MANA_CAPACITY.get();
    }

    @Override
    public int getColor() {
        return 0x7FFF7F;
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), GardenOverflowConfig.DETECTION_RADIUS.get());
    }
}
