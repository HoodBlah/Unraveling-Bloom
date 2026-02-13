# Unraveling Bloom

A Botania addon mod that adds unique functional flowers for crafting manipulation and Corporea logistics.

## About

This mod adds two functional flowers to Botania:

### Unraveling Bloom

The **Unraveling Bloom** is a functional flower that deconstructs crafted items back into their base ingredients, consuming mana in the process.

**Features:**
- **Deconstruction Mechanic**: Throw a crafted item near the flower to uncraft it
- **Mana Cost**: 33,333 mana per uncrafting operation
- **Smart Consumption**: 
  - Recipes with 4+ ingredients: 50% chance to consume one random ingredient
  - Recipes with <4 ingredients: Returns all ingredients
- **Random Tag Selection**: When recipes use tags (like any planks), returns a random item from that tag
- **Container Item Handling**: Automatically skips non-returnable items (buckets, bottles, etc.)
- **Tool Support**: Works with fresh, undamaged tools and armor
- **Sound Effects**: Burp when consuming, anvil sound when uncrafting

**Usage:**
1. Place the flower on grass or dirt
2. Connect it to a mana source (Mana Pool)
3. Throw a crafted item near the flower
4. Wait for it to uncraft (costs 33,333 mana)
5. Collect the returned ingredients

### Logistica Floris

The **Logistica Floris** is a directional logistics flower that exports items from chests to the Corporea network.

**Features:**
- **Chest Export**: Extracts items one at a time from a chest placed in front
- **Corporea Integration**: Requires a Corporea Spark placed on top of the flower
- **Internal Buffer**: Has a 1-slot internal inventory that holds items from the chest
- **Network Access**: Items in the flower's buffer are automatically available to the Corporea network
- **Mana Cost**: 10 mana per item transferred from chest to buffer
- **Transfer Delay**: 20-tick (1 second) cooldown between transfers
- **Automatic Operation**: No player interaction needed once set up

**Usage:**
1. Place the flower facing a chest (the flower will face away from you when placed)
2. Place a Corporea Spark on top of the flower
3. Connect the flower to a mana source (Mana Pool or Mana Spreader)
4. Place items in the chest
5. The flower will automatically pull items into its buffer (10 mana each)
6. Items in the flower's buffer are available to the Corporea network
7. When the network requests matching items, they are pulled from the flower's inventory

### Requirements

- **Minecraft**: 1.20.1
- **Forge**: 47.2.0+
- **Botania**: 1.20.1-446-FORGE or later

## Installation

1. Download the mod JAR from [Releases](https://github.com/abyanlite/unraveling-bloom/releases)
2. Install Minecraft Forge 1.20.1
3. Install Botania 1.20.1-446 or later
4. Place the mod JAR in your `mods` folder
5. Launch the game!

## Usage

1. Craft or obtain an Unraveling Bloom flower
2. Place it on grass or dirt
3. Connect it to a mana source (Mana Pool)
4. Throw a crafted item near the flower
5. Wait for it to uncraft (costs 33,333 mana)
6. Collect the returned ingredients

## Recipe

The Unraveling Bloom can be found in the Botania functional flowers creative tab.

## License

All Rights Reserved

## Credits

Created by **AbyAnLite**

Special thanks to the Botania team for creating an amazing magic mod!
