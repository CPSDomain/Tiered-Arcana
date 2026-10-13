package com.bobroflza.tieredarcana.network;

import com.bobroflza.tieredarcana.client.ClientArcaneData;
import com.bobroflza.tieredarcana.mana.ArcanePlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

/** Server → client: full copy of the player's arcane data (small, sent only when something changed). */
public class SyncArcaneDataPacket {
    public final float mana;
    public final int maxMana;
    public final int shrineTier;
    public final int crystalsUsed;
    public final List<ResourceLocation> known;
    @Nullable public final ResourceLocation active;
    /** Remaining cooldown ticks per spell. */
    public final Map<ResourceLocation, Integer> cooldowns;

    public SyncArcaneDataPacket(float mana, int maxMana, int shrineTier, int crystalsUsed, List<ResourceLocation> known,
                                @Nullable ResourceLocation active, Map<ResourceLocation, Integer> cooldowns) {
        this.mana = mana;
        this.maxMana = maxMana;
        this.shrineTier = shrineTier;
        this.crystalsUsed = crystalsUsed;
        this.known = known;
        this.active = active;
        this.cooldowns = cooldowns;
    }

    public static SyncArcaneDataPacket of(ServerPlayer player, ArcanePlayerData d) {
        long now = player.level().getGameTime();
        Map<ResourceLocation, Integer> cds = new HashMap<>();
        d.getCooldownEnds().forEach((id, end) -> {
            if (end > now) cds.put(id, (int) (end - now));
        });
        return new SyncArcaneDataPacket(d.getMana(), d.getMaxMana(), d.getShrineTier(), d.getCrystalsUsed(),
                new ArrayList<>(d.getKnownSpells()), d.getActiveSpell(), cds);
    }

    public SyncArcaneDataPacket(FriendlyByteBuf buf) {
        mana = buf.readFloat();
        maxMana = buf.readVarInt();
        shrineTier = buf.readVarInt();
        crystalsUsed = buf.readVarInt();
        int n = buf.readVarInt();
        known = new ArrayList<>(n);
        for (int i = 0; i < n; i++) known.add(buf.readResourceLocation());
        active = buf.readBoolean() ? buf.readResourceLocation() : null;
        int c = buf.readVarInt();
        cooldowns = new HashMap<>();
        for (int i = 0; i < c; i++) cooldowns.put(buf.readResourceLocation(), buf.readVarInt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(mana);
        buf.writeVarInt(maxMana);
        buf.writeVarInt(shrineTier);
        buf.writeVarInt(crystalsUsed);
        buf.writeVarInt(known.size());
        known.forEach(buf::writeResourceLocation);
        buf.writeBoolean(active != null);
        if (active != null) buf.writeResourceLocation(active);
        buf.writeVarInt(cooldowns.size());
        cooldowns.forEach((id, t) -> {
            buf.writeResourceLocation(id);
            buf.writeVarInt(t);
        });
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientArcaneData.apply(this));
    }
}
