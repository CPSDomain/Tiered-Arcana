package com.bobroflza.tieredarcana.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ArcanaConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.IntValue BASE_MAX_MANA;
    public static final ForgeConfigSpec.IntValue MANA_PER_CRYSTAL;
    public static final ForgeConfigSpec.IntValue CRYSTALS_PER_SHRINE_TIER;
    public static final ForgeConfigSpec.DoubleValue BASE_REGEN;
    public static final ForgeConfigSpec.DoubleValue SHRINE_REGEN_MULTIPLIER;
    public static final ForgeConfigSpec.DoubleValue NIGHT_REGEN_BONUS;
    public static final ForgeConfigSpec.DoubleValue FULL_MOON_REGEN_BONUS;
    public static final ForgeConfigSpec.IntValue RITUAL_TICKS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.push("mana");
        BASE_MAX_MANA = b.comment("Starting maximum mana for every player").defineInRange("baseMaxMana", 100, 10, 100000);
        MANA_PER_CRYSTAL = b.comment("Max mana gained per Mana Crystal").defineInRange("manaPerCrystal", 20, 1, 10000);
        CRYSTALS_PER_SHRINE_TIER = b.comment("How many Mana Crystals a player may use per shrine tier they have attuned to")
                .defineInRange("crystalsPerShrineTier", 3, 0, 1000);
        BASE_REGEN = b.comment("Mana regenerated per second, passively").defineInRange("regenPerSecond", 1.0, 0.0, 10000.0);
        SHRINE_REGEN_MULTIPLIER = b.comment("Regen multiplier while standing in an active shrine's rune circle (plus 1 per extra shrine tier)")
                .defineInRange("shrineRegenMultiplier", 3.0, 1.0, 1000.0);
        NIGHT_REGEN_BONUS = b.comment("Extra regen fraction outdoors at night (0.25 = +25%)").defineInRange("nightRegenBonus", 0.25, 0.0, 100.0);
        FULL_MOON_REGEN_BONUS = b.comment("Extra regen fraction outdoors during a full moon night").defineInRange("fullMoonRegenBonus", 0.75, 0.0, 100.0);
        b.pop();
        b.push("shrine");
        RITUAL_TICKS = b.comment("How long a spell-learning ritual takes, in ticks").defineInRange("ritualTicks", 100, 20, 2400);
        b.pop();
        SPEC = b.build();
    }

    private ArcanaConfig() {}
}
