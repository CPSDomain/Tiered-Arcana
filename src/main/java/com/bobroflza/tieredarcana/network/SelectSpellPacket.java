package com.bobroflza.tieredarcana.network;

import com.bobroflza.tieredarcana.mana.ArcanePlayerData;
import com.bobroflza.tieredarcana.spell.Spell;
import com.bobroflza.tieredarcana.spell.SpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client → server: the player clicked a learned spell in the spellbook. */
public class SelectSpellPacket {
    private final ResourceLocation spell;

    public SelectSpellPacket(ResourceLocation spell) { this.spell = spell; }
    public SelectSpellPacket(FriendlyByteBuf buf) { this.spell = buf.readResourceLocation(); }
    public void encode(FriendlyByteBuf buf) { buf.writeResourceLocation(spell); }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) return;
        Spell s = SpellRegistry.get(spell);
        if (s == null) return;
        player.getCapability(ArcanePlayerData.CAP).ifPresent(d -> {
            if (d.knows(spell)) {
                d.setActiveSpell(spell);
                player.displayClientMessage(Component.translatable("message.tieredarcana.active_spell", s.getName())
                        .withStyle(ChatFormatting.LIGHT_PURPLE), true);
            }
        });
    }
}
