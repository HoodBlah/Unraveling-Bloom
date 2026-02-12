# Unraveling Bloom - Botania Addon Mod

A Minecraft Forge 1.20.1 mod that adds the **Unraveling Bloom** flower to Botania.

## Features

The Unraveling Bloom is a functional flower that:

- **Detects dropped items** in a 2-block radius
- **Reverses crafting recipes** (uncrafting)
- **Consumes mana** (100,000 per operation from a 200,000 capacity)
- **Randomly removes one ingredient** from the recipe result
- **Spawns remaining ingredients** into the world
- Has a cooldown between operations (100 ticks / 5 seconds)

## Validation & Safety

The flower includes strict validation to prevent exploits:

✅ Rejects damaged or enchanted items
✅ Rejects items with custom NBT data
✅ Rejects recipes with multiple output variants
✅ Rejects tag-based ingredients
✅ Rejects container items (buckets, bowls, etc.)
✅ Rejects recipes with multiple ingredient options
✅ Server-side only processing
✅ No duplication exploits

## Installation

1. Install Forge 1.20.1-47.2.0 or later
2. Install Botania 1.20.1 (version 444+)
3. Place the JAR file in your `mods` folder

## Recipe

Craft the Unraveling Bloom in a Petal Apothecary with:

- 2x Lime Petals
- 2x Purple Petals
- 2x Cyan Petals
- 1x Rune of Mana
- 1x Rune of Earth
- 1x Mana Pearl

## Configuration

The mod includes configurable values in `unraveling_bloom-common.toml`:

```toml
mana_capacity = 200000
mana_cost = 100000
cooldown_ticks = 100
detection_radius = 2
```

## Architecture

- **UnravelingBloomBlockEntity**: Core flower logic with recipe detection and uncrafting
- **UnravelingBloomBlock**: The block definition with proper Botania integration
- **ModBlocks / ModBlockEntities**: Clean Forge registration
- **UnravelingBloomConfig**: Configuration system

## Build

```bash
./gradlew build
```

Output JAR will be in `build/libs/`.

## Compatibility

- **Minecraft**: 1.20.1
- **Forge**: 1.20.1-47.2.0+
- **Botania**: 1.20.1-444+
- **Java**: 17+

## License

All Rights Reserved

## Credits

Created by AbyAnLite
Inspired by Botania's wonderful mod and API
