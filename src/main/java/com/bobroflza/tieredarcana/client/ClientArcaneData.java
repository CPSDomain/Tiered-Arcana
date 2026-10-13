package com.bobroflza.tieredarcana.client;

import com.bobroflza.tieredarcana.network.SyncArcaneDataPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;

/** Client-side mirror of the local player's arcane data, filled by {@link SyncArcaneDataPacket}. */
public final class ClientArcaneData {
    public static float mana;
    public static int maxMana = 100;
    public static int shrineTier;
    public static int crystalsUsed;
    public static final Set<ResourceLocation> KNOWN = new LinkedHashSet<>();
    @Nullable public static ResourceLocation active;
    private static final Map<ResourceLocation, Long> COOLDOWN_ENDS = new HashMap<>();
    private static final Map<ResourceLocation, Integer> COOLDOWN_TOTAL = new HashMap<>();

    private ClientArcaneData() {}

    public static void apply(SyncArcaneDataPacket p) {
        mana = p.mana;
        maxMana = p.maxMana;
        shrineTier = p.shrineTier;
        crystalsUsed = p.crystalsUsed;
        KNOWN.clear();
        KNOWN.addAll(p.known);
        active = p.active;
        long now = now();
        COOLDOWN_ENDS.clear();
        p.cooldowns.forEach((id, remaining) -> {
            COOLDOWN_ENDS.put(id, now + remaining);
            // remember the longest value seen as the "total" for drawing a fill fraction
            COOLDOWN_TOTAL.merge(id, remaining, Math::max);
        });
    }

    /** 0 = ready, 1 = just cast. */
    public static float cooldownFraction(ResourceLocation id) {
        Long end = COOLDOWN_ENDS.get(id);
        if (end == null) return 0;
        long left = end - now();
        if (left <= 0) {
            COOLDOWN_TOTAL.remove(id);
            return 0;
        }
        int total = Math.max(1, COOLDOWN_TOTAL.getOrDefault(id, (int) left));
        return Math.min(1f, left / (float) total);
    }

    private static long now() {
        Minecraft mc = Minecraft.getInstance();
        return mc.level == null ? 0 : mc.level.getGameTime();
    }
}
