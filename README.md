# Tiered Arcana

A Forge 1.20.1 magic mod by BobRoflza. You build the Void Crystal Shrine and grow it tier by tier, learn spells into a spellbook, and cast them with a wand using mana.

The full player guide is in `docs/index.html`.

## Building
Push to `main` and the GitHub Actions workflow builds the jar and publishes a release named `v<mod_version>`, taken from `gradle.properties`.

To build locally, run `./gradlew build`. It needs JDK 17, and the jar ends up in `build/libs/`.

## Status (v0.1.0)
- Mana system with a HUD bar. Regen comes from passive regen, the shrine circle and moonlight; you can also use potions, a Mana Gem and Mana Crystals.
- Spellbook GUI with school tabs. Clicking a spell sets it active, `V` cycles spells, and sneak + scroll also cycles while holding a wand.
- Apprentice Wand.
- 12 spells are implemented. Tier 1 and Tier 2 scrolls can be crafted.
- Tier 1 shrine: multiblock check, ghost-block preview while holding a wand, spell-learning ritual, regen circle and Mana Gem charging.

## Next
Tier 2 shrine (Catalyst Pylons, Infused Gems, Adept Wand), then Tier 3 (Obelisks, Void Crystals), then Tier 4 (Ascended, Astral Essence).
