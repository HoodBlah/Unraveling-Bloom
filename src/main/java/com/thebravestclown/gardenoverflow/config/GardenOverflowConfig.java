package com.thebravestclown.gardenoverflow.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class GardenOverflowConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue MANA_CAPACITY;
    public static final ModConfigSpec.IntValue MANA_COST;
    public static final ModConfigSpec.IntValue COOLDOWN_TICKS;
    public static final ModConfigSpec.IntValue DETECTION_RADIUS;
    public static final ModConfigSpec.IntValue CONSUME_CHANCE_PERCENT;
    public static final ModConfigSpec.IntValue LOSS_MIN_INGREDIENTS;
    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING;

    public static final ModConfigSpec.IntValue LOGISTICA_MANA_CAPACITY;
    public static final ModConfigSpec.IntValue LOGISTICA_MANA_PER_ITEM;
    public static final ModConfigSpec.IntValue LOGISTICA_MAX_ITEMS;
    public static final ModConfigSpec.IntValue LOGISTICA_DELAY_TICKS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("unraveling_bloom");
        MANA_CAPACITY = b.comment("Maximum mana the flower can hold (keep it above mana_cost)")
                .defineInRange("mana_capacity", 200_000, 10_000, 1_000_000);
        MANA_COST = b.comment("Mana cost per uncraft")
                .defineInRange("mana_cost", 33_333, 1_000, 500_000);
        COOLDOWN_TICKS = b.comment("Ticks between uncrafts (20 = 1 second)")
                .defineInRange("cooldown_ticks", 100, 1, 600);
        DETECTION_RADIUS = b.comment("Radius in blocks to look for thrown items")
                .defineInRange("detection_radius", 2, 1, 10);
        CONSUME_CHANCE_PERCENT = b.comment("Chance (%) that one random ingredient is lost during an uncraft")
                .defineInRange("consume_chance_percent", 10, 0, 100);
        LOSS_MIN_INGREDIENTS = b.comment("Only recipes with at least this many ingredients can lose one")
                .defineInRange("loss_min_ingredients", 5, 1, 9);
        DEBUG_LOGGING = b.comment("Log why the flower accepts or skips items (latest.log)")
                .define("debug_logging", false);
        b.pop();

        b.push("logistica_floris");
        LOGISTICA_MANA_CAPACITY = b.comment("Maximum mana the flower can hold (keep it above mana_per_item * max_items_per_transfer)")
                .defineInRange("mana_capacity", 1_000, 100, 1_000_000);
        LOGISTICA_MANA_PER_ITEM = b.comment("Mana cost per item moved")
                .defineInRange("mana_per_item", 10, 1, 10_000);
        LOGISTICA_MAX_ITEMS = b.comment("Maximum items moved per transfer")
                .defineInRange("max_items_per_transfer", 24, 1, 64);
        LOGISTICA_DELAY_TICKS = b.comment("Ticks between transfers (20 = 1 second)")
                .defineInRange("transfer_delay_ticks", 20, 1, 600);
        b.pop();
        SPEC = b.build();
    }

    private GardenOverflowConfig() {}
}
