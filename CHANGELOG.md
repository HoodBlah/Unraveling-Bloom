# Changelog

## [1.1.0] - 2026-02-11

### Added
- Logistica Floris functional flower for Corporea logistics
- Directional chest item extraction
- Corporea Spark integration (required on top of flower)
- Automatic item transfer to Corporea network
- 10 mana per item transfer cost
- 20-tick (1 second) transfer delay
- Automatic item return to chest on network failure
- Purple/gold flower texture with arrow-like petals
- Directional placement (flower faces away from player)

### Features
- Works with any Container block (chests, barrels, etc.)
- Transfers one item at a time
- Maximum mana capacity: 1,000
- Requires Corporea Spark on block above
- Detects chest in direction flower is facing

### Known Issues
- Corporea network insertion is placeholder (currently just consumes items)
- Need to research proper Botania Corporea API for item insertion

## [1.0.0] - 2026-02-11

### Added
- Initial release of Unraveling Bloom
- Unraveling Bloom functional flower that uncrafts items
- Mana-powered deconstruction system (33,333 mana per operation)
- Smart ingredient consumption (50% chance for 4+ ingredient recipes)
- Support for tools, armor, and items with harmless NBT data
- Container item filtering (skips buckets, bottles, etc.)
- Sound effects (burp for consumption, anvil for uncrafting)
- Purple flower texture with inward-curling petals
- Full integration with Botania's creative tabs
- Transparent rendering in-game
- Random tag ingredient selection (crafting tables return mixed plank types)

### Features
- Works with all vanilla and modded crafting recipes
- Accepts tagged ingredients (#planks, #logs, etc.)
- Skips empty crafting slots automatically
- Rejects damaged, enchanted, or custom-named items
- Maximum mana capacity: 200,000
- Detection radius: 2 blocks
- Mana pull range: 6 blocks from nearby pools
- Cooldown: 100 ticks between operations
