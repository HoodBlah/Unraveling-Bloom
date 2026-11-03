# Unraveling Bloom (Garden Overflow)

A Botania addon with two functional flowers. Built for **Minecraft 1.21.1 / NeoForge** with
[Botania Revived](https://github.com/Velron-Devs/Botania-Revived).

## Unraveling Bloom
Throw a crafted item within 2 blocks and it is broken back into its recipe ingredients.
- Costs 33,333 mana per uncraft, 5 second cooldown, holds up to 200,000 mana.
- If an item has several crafting recipes, one is picked at random (recipes that use the finished item itself, like NBT-reset crafts, are ignored).
- Tag ingredients return a random matching item. Container items (buckets, bottles) are not returned.
- Recipes with 5+ ingredients have a 10% chance to lose one random ingredient (burp sound). Chance and minimum are configurable.
- Stacks work: each uncraft consumes one craft's worth of items (e.g. 4 sticks), the rest stays.
- Rejects damaged items and items with custom name, lore or enchantments (compared against the item defaults).
- All numbers are in `config/gardenoverflow-common.toml`. Set `debug_logging = true` to see in latest.log why an item was accepted or skipped.

## Logistica Floris
Moves items from an adjacent container into the best inventory on its Corporea network.
- Place a Corporea Spark on the flower, put a chest next to it.
- Defaults: up to 24 items per second, 10 mana per item, holds 1,000 mana. All configurable in `config/gardenoverflow-common.toml` under `[logistica_floris]`.
- Prefers the inventory that already holds the most of that item, else the first with a free slot.

## Mana
Bind either flower to a Mana Pool with the Wand of the Forest (shift-click the flower, then the pool).
An unbound flower automatically binds to the closest pool within 10 blocks. No mana means no uncrafting.

## Building
1. Build Botania Revived from its repo and put the jar in `libs/`.
2. `./gradlew build` (Java 21). The jar is in `build/libs/`.

## License
All Rights Reserved. Created by AbyAnLite / TheBravestClown.
