package com.thebravestclown.gardenoverflow.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class GardenOverflowConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue MANA_CAPACITY;
    public static final ForgeConfigSpec.IntValue MANA_COST;
    public static final ForgeConfigSpec.IntValue COOLDOWN_TICKS;
    public static final ForgeConfigSpec.IntValue DETECTION_RADIUS;

    static {
        BUILDER.push("unraveling_bloom");

        MANA_CAPACITY = BUILDER
                .comment("Maximum mana capacity of the flower")
                .defineInRange("mana_capacity", 200000, 10000, 1000000);

        MANA_COST = BUILDER
                .comment("Mana cost per uncraft operation")
                .defineInRange("mana_cost", 100000, 10000, 500000);

        COOLDOWN_TICKS = BUILDER
                .comment("Cooldown in ticks between operations (20 ticks = 1 second)")
                .defineInRange("cooldown_ticks", 100, 1, 600);

        DETECTION_RADIUS = BUILDER
                .comment("Radius in blocks to detect items")
                .defineInRange("detection_radius", 2, 1, 10);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
