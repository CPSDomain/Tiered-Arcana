package com.bobroflza.tieredarcana.shrine;

import com.bobroflza.tieredarcana.TieredArcana;
import com.bobroflza.tieredarcana.registry.ModBlocks;
import com.bobroflza.tieredarcana.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The Void Crystal Shrine multiblock, defined tier by tier.
 * All offsets are relative to the Void Crystal Core block (the floating crystal).
 * Each tier only lists the blocks it ADDS, so the shrine grows outward/upward as you upgrade it.
 *
 * <pre>
 * Floor (y = -3), top view. Tier 1 is the inner 5x5, Tier 2 adds the outer two rings.
 *
 *   R R R R R R R R R        R = Arcane Runestone
 *   R B B B B B B B R        B = shrine base (deepslate bricks etc.)
 *   R B b b r b b B R        b/r = Tier 1 floor
 *   R B b b r b b B R        P = Catalyst Pylon column on top of this base block:
 *   R B r r r r r B R              y=-2 Runestone, y=-1 Runestone, y=0 Catalyst Crystal
 *   R B b b r b b B R
 *   R B b b r b b B R        Pedestal at (0,-2,0), air gap at (0,-1,0), Core at (0,0,0).
 *   R B B B B B B B R        Pylons stand on the four inner corners of the B ring: (±3, ±3).
 *   R R R R R R R R R
 * </pre>
 *
 * A tier is <b>built</b> when its blocks are all present, and <b>awakened</b> when its key offering has been
 * placed on the pedestal and the crystal activated. Tier 1 needs no offering.
 */
public final class ShrineStructure {
    public static final TagKey<Block> SHRINE_BASE = TagKey.create(Registries.BLOCK, TieredArcana.id("shrine_base"));
    public static final int MAX_TIER = 4;

    public static final BlockPos PEDESTAL_OFFSET = new BlockPos(0, -2, 0);

    /** Catalyst Crystals (Tier 2+): ingredients for advanced rituals are placed on these. */
    public static final List<BlockPos> CATALYST_OFFSETS = List.of(
            new BlockPos(-3, 0, -3), new BlockPos(3, 0, -3), new BlockPos(3, 0, 3), new BlockPos(-3, 0, 3));

    /** The Void Crystal caps on top of the two Tier 3 obelisks – beams of energy flow from here to the core. */
    public static final List<BlockPos> OBELISK_TOPS = List.of(new BlockPos(-4, 4, 0), new BlockPos(4, 4, 0));
    /** The Astral Crystals crowning the obelisk spires at Tier 4 – beams flow from here instead. */
    public static final List<BlockPos> SPIRE_TOPS = List.of(new BlockPos(-4, 6, 0), new BlockPos(4, 6, 0));

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
                t1.add(x == 0 || z == 0 ? runestone(x, -3, z) : base(x, -3, z));
            }
        }
        t1.add(new Entry(PEDESTAL_OFFSET, s -> s.is(ModBlocks.ARCANE_PEDESTAL.get()),
                () -> ModBlocks.ARCANE_PEDESTAL.get().defaultBlockState(), false));
        TIERS.add(Collections.unmodifiableList(t1));

        // ---------------- Tier 2: Pylons ----------------
        List<Entry> t2 = new ArrayList<>();
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                int ring = Math.max(Math.abs(x), Math.abs(z));
                if (ring == 4) t2.add(runestone(x, -3, z));
                else if (ring == 3) t2.add(base(x, -3, z));
            }
        }
        for (BlockPos c : CATALYST_OFFSETS) {
            t2.add(runestone(c.getX(), -2, c.getZ()));
            t2.add(runestone(c.getX(), -1, c.getZ()));
            t2.add(new Entry(c, s -> s.is(ModBlocks.CATALYST_CRYSTAL.get()),
                    () -> ModBlocks.CATALYST_CRYSTAL.get().defaultBlockState(), false));
        }
        TIERS.add(Collections.unmodifiableList(t2));

        // ---------------- Tier 3: Obelisks ----------------
        // Two towers standing on the outer rune border, east and west of the core (x = ±4, z = -1..1).
        //   y=4        V            V = Void Crystal Block
        //   y=3        R            R = Arcane Runestone
        //   y=2        B            B = shrine base
        //   y=1      B V B
        //   y=0      B R B          (level with the core)
        //   y=-1     B B B
        //   y=-2     B B B          (standing on the Tier 2 rune border)
        List<Entry> t3 = new ArrayList<>();
        for (int x : new int[]{-4, 4}) {
            for (int y = -2; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (z == 0 && y == 0) t3.add(runestone(x, y, z));
                    else if (z == 0 && y == 1) t3.add(voidBlock(x, y, z));
                    else t3.add(base(x, y, z));
                }
            }
            t3.add(base(x, 2, 0));
            t3.add(runestone(x, 3, 0));
            t3.add(voidBlock(x, 4, 0));
        }
        TIERS.add(Collections.unmodifiableList(t3));

        // ---------------- Tier 4: Ascended ----------------
        // An octagonal outer ring around the 9x9 floor (radius 5, corners cut), with runestones at the
        // 4 compass points and the 8 cut-corner edges, plus a spire on each obelisk:
        //   y=6  A   Astral Crystal
        //   y=5  R   Arcane Runestone   (on top of the obelisk's Void Crystal cap)
        List<Entry> t4 = new ArrayList<>();
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                if (Math.max(Math.abs(x), Math.abs(z)) != 5) continue;
                int d = Math.abs(x) + Math.abs(z);
                if (d > 8) continue; // cut corners -> octagon
                boolean rune = x == 0 || z == 0 || d == 8;
                t4.add(rune ? runestone(x, -3, z) : base(x, -3, z));
            }
        }
        for (int x : new int[]{-4, 4}) {
            t4.add(runestone(x, 5, 0));
            t4.add(new Entry(new BlockPos(x, 6, 0), s -> s.is(ModBlocks.ASTRAL_CRYSTAL.get()),
                    () -> ModBlocks.ASTRAL_CRYSTAL.get().defaultBlockState(), false));
        }
        TIERS.add(Collections.unmodifiableList(t4));
    }

    private static Entry voidBlock(int x, int y, int z) {
        return new Entry(new BlockPos(x, y, z), s -> s.is(ModBlocks.VOID_CRYSTAL_BLOCK.get()),
                () -> ModBlocks.VOID_CRYSTAL_BLOCK.get().defaultBlockState(), false);
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

    /** Highest fully BUILT tier, 0 if even tier 1 is incomplete. */
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

    /** The item that must be offered on the pedestal to awaken a built tier; null = no offering needed. */
    @Nullable
    public static Item keyOffering(int tier) {
        return switch (tier) {
            case 2 -> ModItems.INFUSED_GEM.get();
            case 3 -> ModItems.VOID_CRYSTAL.get();
            case 4 -> ModItems.ASTRAL_ESSENCE.get();
            default -> null;
        };
    }

    /** Runestones of all built tiers, in build order – lit up one by one during a ritual. */
    public static List<BlockPos> runes(BlockPos core, int upToTier) {
        List<BlockPos> list = new ArrayList<>();
        for (int t = 1; t <= upToTier; t++) {
            for (Entry e : entries(t)) if (e.rune()) list.add(core.offset(e.offset()));
        }
        return list;
    }

    /** Half-width of the rune circle players stand in for the regen boost. */
    public static int circleRadius(int tier) {
        return tier <= 1 ? 2 : tier <= 3 ? 4 : 5;
    }
}
