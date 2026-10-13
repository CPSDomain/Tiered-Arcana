package com.bobroflza.tieredarcana.registry;

import com.bobroflza.tieredarcana.TieredArcana;
import com.bobroflza.tieredarcana.item.*;
import com.bobroflza.tieredarcana.spell.Spell;
import com.bobroflza.tieredarcana.spell.SpellRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Every item here is obtainable through a plain crafting-table recipe (shaped/shapeless, no NBT on outputs),
 * so they all work with JEI's "+" transfer and with crafting patterns in Tiered Networks / Tiered Industries.
 */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TieredArcana.MODID);

    // ---- materials ----
    public static final RegistryObject<Item> ARCANE_DUST = ITEMS.register("arcane_dust", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> INFUSED_GEM = ITEMS.register("infused_gem",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ---- magic gear ----
    public static final RegistryObject<Item> SPELLBOOK = ITEMS.register("spellbook",
            () -> new SpellbookItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    /** Tier 1 wand: Tier 1 spells, full mana cost, 100% power. */
    public static final RegistryObject<Item> APPRENTICE_WAND = ITEMS.register("apprentice_wand",
            () -> new WandItem(new Item.Properties(), 1, 1, 1.0f, 1.0f));
    /** Tier 2 wand: Tier 2 spells, -10% mana cost, 125% power. */
    public static final RegistryObject<Item> ADEPT_WAND = ITEMS.register("adept_wand",
            () -> new WandItem(new Item.Properties().rarity(Rarity.UNCOMMON), 2, 2, 0.9f, 1.25f));

    // ---- scrolls (one item per spell) ----
    public static final RegistryObject<Item> BLANK_SCROLL = ITEMS.register("blank_scroll", () -> new Item(new Item.Properties()));
    public static final Map<Spell, RegistryObject<Item>> SCROLLS = new LinkedHashMap<>();

    static {
        for (Spell spell : SpellRegistry.all()) {
            SCROLLS.put(spell, ITEMS.register("scroll_" + spell.getId().getPath(),
                    () -> new SpellScrollItem(new Item.Properties(), spell)));
        }
    }

    // ---- mana ----
    public static final RegistryObject<Item> MINOR_MANA_POTION = ITEMS.register("minor_mana_potion",
            () -> new ManaPotionItem(new Item.Properties(), 30));
    public static final RegistryObject<Item> MANA_POTION = ITEMS.register("mana_potion",
            () -> new ManaPotionItem(new Item.Properties().rarity(Rarity.UNCOMMON), 60));
    public static final RegistryObject<Item> GREATER_MANA_POTION = ITEMS.register("greater_mana_potion",
            () -> new ManaPotionItem(new Item.Properties().rarity(Rarity.RARE), 120));
    public static final RegistryObject<Item> MANA_CRYSTAL = ITEMS.register("mana_crystal",
            () -> new ManaCrystalItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> MANA_GEM = ITEMS.register("mana_gem",
            () -> new ManaGemItem(new Item.Properties().rarity(Rarity.UNCOMMON), 200));

    // ---- block items ----
    public static final RegistryObject<Item> ARCANE_ORE = block("arcane_ore", ModBlocks.ARCANE_ORE);
    public static final RegistryObject<Item> DEEPSLATE_ARCANE_ORE = block("deepslate_arcane_ore", ModBlocks.DEEPSLATE_ARCANE_ORE);
    public static final RegistryObject<Item> ARCANE_RUNESTONE = block("arcane_runestone", ModBlocks.ARCANE_RUNESTONE);
    public static final RegistryObject<Item> ARCANE_PEDESTAL = block("arcane_pedestal", ModBlocks.ARCANE_PEDESTAL);
    public static final RegistryObject<Item> CATALYST_CRYSTAL = block("catalyst_crystal", ModBlocks.CATALYST_CRYSTAL);
    public static final RegistryObject<Item> VOID_CRYSTAL_CORE = ITEMS.register("void_crystal_core",
            () -> new BlockItem(ModBlocks.VOID_CRYSTAL_CORE.get(), new Item.Properties().rarity(Rarity.RARE)));

    private static RegistryObject<Item> block(String name, RegistryObject<Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static Item scroll(Spell spell) {
        return SCROLLS.get(spell).get();
    }

    private ModItems() {}
}
