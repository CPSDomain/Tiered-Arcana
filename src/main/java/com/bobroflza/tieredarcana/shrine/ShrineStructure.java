package com.bobroflza.tieredarcana.shrine;

import com.bobroflza.tieredarcana.TieredArcana;
import com.bobroflza.tieredarcana.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The Void Crystal Shrine multiblock, defined tier by tier.
 * All offsets are relative to the Void Crystal Core block (the floating crystal).
 * A shrine is tier N when tiers 1..N are all complete. Each tier only lists the blocks it ADDS,
 * so the shrine physically grows outward/upward as you upgrade it.
 *
 * <pre>
 * Tier 1 – Arcane Circle (side view)          Tier 1 floor (top view, y = -3)
 *
 *   y= 0        ◆   Void Crystal Core           B B R B B
 *   y=-1        .   (air gap)                   B B R B B
 *   y=-2        P   Arcane Pedestal             R R R R R      R = Arcane Runestone
 *   y=-3    B B R B B   floor                   B B R B B      B = shrine base (deepslate bricks etc.)
 *                                               B B R B B
 * </pre>
 */
public final class ShrineStructure {
    public static final TagKey<Block> SHRINE_BASE = TagKey.create(Registries.BLOCK, TieredArcana.id("shrine_base"));
    public static final int MAX_TIER = 1; // Tiers 2-4 (Pylons, Obelisks, Ascended) are added in later updates

    public static final BlockPos PEDESTAL_OFFSET = new BlockPos(0, -2, 0);

    /** One required block: where, what's accepted, and what to show as a ghost preview. */
    public record Entry(BlockPos offset, Predicate<BlockState> test, Supplier<BlockState> display, boolean rune) {
        public boolean matches(BlockGetter level, BlockPos core) {
            return test.test(level.getBlockState(core.offset(offset)));
        }
    }

    private static final List<List<Entry>> TIERS = new ArrayList<>();

    static {
        // ---------------- Tier 1: Arcane Circle ----------------
        List<Entry> t1 = new ArrayList<>();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                boolean cross = x == 0 || z == 0;
                t1.add(cross ? runestone(x, -3, z) : base(x, -3, z));
            }
        }
        t1.add(new Entry(PEDESTAL_OFFSET, s -> s.is(ModBlocks.ARCANE_PEDESTAL.get()),
                () -> ModBlocks.ARCANE_PEDESTAL.get().defaultBlockState(), false));
        TIERS.add(Collections.unmodifiableList(t1));
    }

    private static Entry runestone(int x, int y, int z) {
        return new Entry(new BlockPos(x, y, z), s -> s.is(ModBlocks.ARCANE_RUNESTONE.get()),
                () -> ModBlocks.ARCANE_RUNESTONE.get().defaultBlockState(), true);
    }

    private static Entry base(int x, int y, int z) {
        return new Entry(new BlockPos(x, y, z), s -> s.is(SHRINE_BASE),
                Blocks.DEEPSLATE_BRICKS::defaultBlockState, false);
    }

    private ShrineStructure() {}

    /** Blocks added by the given tier (1-based). Empty if that tier doesn't exist yet. */
    public static List<Entry> entries(int tier) {
        if (tier < 1 || tier > TIERS.size()) return List.of();
        return TIERS.get(tier - 1);
    }

    /** Highest fully built tier, 0 if even tier 1 is incomplete. */
    public static int computeTier(BlockGetter level, BlockPos core) {
        int tier = 0;
        for (List<Entry> entries : TIERS) {
            for (Entry e : entries) {
                if (!e.matches(level, core)) return tier;
            }
            tier++;
        }
        return tier;
    }

    public static int countMissing(BlockGetter level, BlockPos core, int tier) {
        int missing = 0;
        for (Entry e : entries(tier)) if (!e.matches(level, core)) missing++;
        return missing;
    }

    /** Runestones of all built tiers, in build order – lit up one by one during a ritual. */
    public static List<BlockPos> runes(BlockPos core, int upToTier) {
        List<BlockPos> list = new ArrayList<>();
        for (int t = 1; t <= upToTier; t++) {
            for (Entry e : entries(t)) if (e.rune()) list.add(core.offset(e.offset()));
        }
        return list;
    }

    /** Half-width of the rune circle players stand in for the regen boost and to receive a ritual. */
    public static int circleRadius(int tier) {
        return tier <= 1 ? 2 : 4;
    }
}
