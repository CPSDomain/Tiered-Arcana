package com.bobroflza.tieredarcana.network;

import com.bobroflza.tieredarcana.mana.ArcanePlayerData;
import com.bobroflza.tieredarcana.spell.Spell;
import com.bobroflza.tieredarcana.spell.SpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Client → server: cycle the active spell forwards/backwards through the learned spells (book order). */
public class CycleSpellPacket {
    private final boolean forward;

    public CycleSpellPacket(boolean forward) { this.forward = forward; }
    public CycleSpellPacket(FriendlyByteBuf buf) { this.forward = buf.readBoolean(); }
    public void encode(FriendlyByteBuf buf) { buf.writeBoolean(forward); }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return;
        player.getCapability(ArcanePlayerData.CAP).ifPresent(d -> {
            List<ResourceLocation> order = new ArrayList<>();
            for (Spell s : SpellRegistry.all()) if (d.knows(s.getId())) order.add(s.getId());
            if (order.isEmpty()) return;
            int idx = order.indexOf(d.getActiveSpell());
            int next = idx < 0 ? 0 : Math.floorMod(idx + (forward ? 1 : -1), order.size());
            ResourceLocation id = order.get(next);
            d.setActiveSpell(id);
            Spell s = SpellRegistry.get(id);
            if (s != null) {
                player.displayClientMessage(Component.translatable("message.tieredarcana.active_spell", s.getName())
                        .withStyle(ChatFormatting.LIGHT_PURPLE), true);
            }
        });
    }
}
