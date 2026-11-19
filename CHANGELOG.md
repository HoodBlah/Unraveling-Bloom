# Changelog

## [1.2.1] - Logistica Floris stall fix
- Logistica Floris no longer gets stuck on the first occupied chest slot when the network cannot accept that item; the remaining slots are still processed.
- Leftovers spill into the next best inventory once the preferred one is full, instead of being returned to the chest.
- A pass that moves nothing now backs off 10 ticks instead of rescanning every tick.

## [1.2.0] - Minecraft 1.21.1 / NeoForge port
- Ported from Forge 1.20.1 to NeoForge 1.21.1 and Botania Revived.
- Flowers now use Botania's own `SpecialFlowerBlock` and mana binding (wand binding works, radius overlay and mana HUD shown).
- Removed the per-tick 13x13x13 pool scan; flowers use the bound pool instead.
- Logistica Floris finds Corporea inventories through the network instead of scanning 33x33x33 blocks.
- Config values are now actually used (they were ignored before); added `consume_chance_percent`.
- Added loot tables (the flowers dropped nothing when broken).
- Removed dead code (empty menu registry, unused block classes, debug prints, missing-texture item model).
- Unraveling Bloom: stacks are handled (one craft's worth consumed at a time), unbound flowers auto-bind to the nearest pool, loss needs `loss_min_ingredients` (default 5) and `consume_chance_percent`, new `debug_logging` option.
- Logistica Floris: mana capacity, mana per item, items per transfer and delay are now configurable.
- Unraveling Bloom picks randomly between multiple recipes for the same item; self-referential (reset) recipes are ignored.
- Repo cleaned of build output (`build/`, `.gradle/`, `run/`).

## [1.1.0] - Logistica Floris
## [1.0.0] - Initial release (Unraveling Bloom)
