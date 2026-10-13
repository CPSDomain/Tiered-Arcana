# Tiered Arcana

A Forge 1.20.1 magic mod by BobRoflza. You build the Void Crystal Shrine and grow it tier by tier, learn spells into a spellbook, and cast them with a wand using mana.

The full player guide is in `docs/index.html`.

## Building
Push to `main` and the GitHub Actions workflow builds the jar and publishes a release named `v<mod_version>`, taken from `gradle.properties`.

To build locally, run `./gradlew build`. It needs JDK 17, and the jar ends up in `build/libs/`.

## Status (v0.3.0)
- Mana system with a HUD bar. Regen comes from passive regen, the shrine circle and moonlight; you can also use potions, a Mana Gem and Mana Crystals.
- Spellbook GUI with school tabs. Clicking a spell sets it active, `V` cycles spells, and sneak + scroll also cycles while holding a wand.
- Apprentice Wand.
- 12 spells are implemented. Tier 1 and Tier 2 scrolls can be crafted.
- Tier 1 shrine: multiblock check, ghost-block preview while holding a wand, spell-learning ritual, regen circle and Mana Gem charging.
- Tier 2 shrine: 9×9 floor, 4 Catalyst Pylons, and an Infused Gem key offering to awaken it. Tier 2 spells need catalyst rituals.
- Adept Wand (Tier 2 spells, −10% mana cost, 125% power).
- Tier 3 shrine: two obelisks on the east and west border, awakened with a Void Crystal. Obelisk energy beams flow into the core.
- Archmage Wand (Tier 3 spells, −20% mana cost, 150% power). Void Crystal and Block of Void Crystal. Tier 3 scrolls can be crafted.
- Every item has a plain crafting recipe with no NBT on outputs. That keeps them compatible with JEI's + transfer and with crafting patterns in Tiered Networks and Tiered Industries. Each spell has its own scroll item.

## Next
Tier 4 (Ascended: stepped base, spire crystals, Astral Essence, Astral Wand, Meteor and Void Rift).
