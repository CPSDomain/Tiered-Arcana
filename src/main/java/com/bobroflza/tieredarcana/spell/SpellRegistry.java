package com.bobroflza.tieredarcana.spell;

import com.bobroflza.tieredarcana.TieredArcana;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.*;

/**
 * All spells, in spellbook order. Spells are plain Java objects (not a Forge registry) –
 * they're identified by ResourceLocation in saves and packets so adding more later is safe.
 */
public final class SpellRegistry {
    private static final Map<ResourceLocation, Spell> SPELLS = new LinkedHashMap<>();

    @FunctionalInterface
    public interface Caster {
        boolean cast(ServerPlayer player, ServerLevel level, float power);
    }

    private static Spell register(String name, School school, int tier, int mana, int cooldown, Caster caster) {
        ResourceLocation id = TieredArcana.id(name);
        Spell spell = new Spell(id, school, tier, mana, cooldown) {
            @Override
            public boolean cast(ServerPlayer player, ServerLevel level, float power) {
                return caster.cast(player, level, power);
            }
        };
        SPELLS.put(id, spell);
        return spell;
    }

    // ---------------- Tier 1 ----------------
    public static final Spell MAGIC_MISSILE = register("magic_missile", School.ARCANE, 1, 8, 10, Spells::magicMissile);
    public static final Spell LIGHT_ORB = register("light_orb", School.ARCANE, 1, 5, 10, Spells::lightOrb);
    public static final Spell MINOR_HEAL = register("minor_heal", School.NATURE, 1, 15, 60, Spells::minorHeal);
    // ---------------- Tier 2 ----------------
    public static final Spell FIREBALL = register("fireball", School.FIRE, 2, 20, 30, Spells::fireball);
    public static final Spell FROST_NOVA = register("frost_nova", School.FROST, 2, 25, 80, Spells::frostNova);
    public static final Spell BLINK = register("blink", School.VOID, 2, 20, 40, Spells::blink);
    public static final Spell GROWTH = register("growth", School.NATURE, 2, 15, 40, Spells::growth);
    // ---------------- Tier 3 ----------------
    public static final Spell LIGHTNING_STRIKE = register("lightning_strike", School.ARCANE, 3, 35, 100, Spells::lightning);
    public static final Spell VOID_SHIELD = register("void_shield", School.VOID, 3, 40, 400, Spells::voidShield);
    public static final Spell LEVITATE = register("levitate", School.ARCANE, 3, 20, 200, Spells::levitate);
    // ---------------- Tier 4 ----------------
    public static final Spell METEOR = register("meteor", School.FIRE, 4, 60, 300, Spells::meteor);
    public static final Spell VOID_RIFT = register("void_rift", School.VOID, 4, 60, 300, Spells::voidRift);

    private SpellRegistry() {}

    @Nullable
    public static Spell get(@Nullable ResourceLocation id) {
        return id == null ? null : SPELLS.get(id);
    }

    public static Collection<Spell> all() {
        return Collections.unmodifiableCollection(SPELLS.values());
    }

    public static List<Spell> bySchool(School school) {
        List<Spell> list = new ArrayList<>();
        for (Spell s : SPELLS.values()) if (s.getSchool() == school) list.add(s);
        return list;
    }
}
