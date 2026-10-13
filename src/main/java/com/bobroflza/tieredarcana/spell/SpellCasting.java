package com.bobroflza.tieredarcana.spell;

import com.bobroflza.tieredarcana.item.WandItem;
import com.bobroflza.tieredarcana.mana.ArcanePlayerData;
import com.bobroflza.tieredarcana.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;

public final class SpellCasting {
    private SpellCasting() {}

    public static boolean hasSpellbook(ServerPlayer player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).is(ModItems.SPELLBOOK.get())) return true;
        }
        return false;
    }

    private static void fail(ServerPlayer player, Component msg) {
        player.displayClientMessage(msg.copy().withStyle(ChatFormatting.RED), true);
    }

    /** Called when the player right-clicks with a wand. Returns true if a spell was cast. */
    public static boolean tryCast(ServerPlayer player, WandItem wand) {
        ArcanePlayerData data = player.getCapability(ArcanePlayerData.CAP).orElse(null);
        if (data == null) return false;

        if (!hasSpellbook(player)) {
            fail(player, Component.translatable("message.tieredarcana.need_book"));
            return false;
        }
        Spell spell = SpellRegistry.get(data.getActiveSpell());
        if (spell == null || !data.knows(spell.getId())) {
            fail(player, Component.translatable("message.tieredarcana.no_spell"));
            return false;
        }
        if (spell.getTier() > wand.getMaxSpellTier()) {
            fail(player, Component.translatable("message.tieredarcana.wand_too_weak", spell.getTier()));
            return false;
        }
        long now = player.level().getGameTime();
        if (data.getCooldownEnd(spell.getId()) > now) {
            long secs = (data.getCooldownEnd(spell.getId()) - now + 19) / 20;
            fail(player, Component.translatable("message.tieredarcana.cooldown", spell.getName(), secs));
            return false;
        }
        float cost = spell.getManaCost() * wand.getCostMultiplier();
        if (data.getMana() < cost) {
            fail(player, Component.translatable("message.tieredarcana.no_mana"));
            return false;
        }
        if (!spell.cast(player, player.serverLevel(), wand.getPower())) {
            fail(player, Component.translatable("message.tieredarcana.fizzle", spell.getName()));
            return false;
        }
        data.consumeMana(cost);
        data.setCooldownEnd(spell.getId(), now + spell.getCooldownTicks());
        return true;
    }
}
