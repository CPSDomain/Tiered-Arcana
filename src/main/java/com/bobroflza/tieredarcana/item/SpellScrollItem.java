package com.bobroflza.tieredarcana.item;

import com.bobroflza.tieredarcana.spell.Spell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A scroll holding one spell. Each spell has its OWN scroll item (no NBT), so scrolls work cleanly
 * with JEI's recipe transfer, crafting patterns and autocrafting in other mods.
 * Learned by placing it on the shrine pedestal and running a ritual.
 */
public class SpellScrollItem extends Item {
    private final Spell spell;

    public SpellScrollItem(Properties props, Spell spell) {
        super(props.stacksTo(16).rarity(rarityFor(spell)));
        this.spell = spell;
    }

    private static Rarity rarityFor(Spell s) {
        return switch (s.getTier()) {
            case 1 -> Rarity.COMMON;
            case 2 -> Rarity.UNCOMMON;
            case 3 -> Rarity.RARE;
            default -> Rarity.EPIC;
        };
    }

    public Spell getSpell() { return spell; }

    @Nullable
    public static Spell getSpell(ItemStack stack) {
        return stack.getItem() instanceof SpellScrollItem scroll ? scroll.spell : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tieredarcana.scroll.info", spell.getSchool().getName(), spell.getTier())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(spell.getDescription().copy().withStyle(ChatFormatting.DARK_GRAY));
        if (!spell.getCatalysts().isEmpty()) {
            tooltip.add(Component.translatable("tooltip.tieredarcana.scroll.catalysts").withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(catalystList(spell).withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("tooltip.tieredarcana.scroll.learn", spell.getTier()).withStyle(ChatFormatting.DARK_PURPLE));
    }

    /** "2× Blaze Powder, Magma Cream, Fire Charge" */
    public static MutableComponent catalystList(Spell spell) {
        MutableComponent out = Component.literal(" ");
        List<Item> cats = spell.getCatalysts();
        boolean first = true;
        for (int i = 0; i < cats.size(); i++) {
            Item item = cats.get(i);
            if (cats.subList(0, i).contains(item)) continue; // already counted
            int count = 0;
            for (Item c : cats) if (c == item) count++;
            if (!first) out.append(", ");
            if (count > 1) out.append(count + "× ");
            out.append(item.getDescription());
            first = false;
        }
        return out;
    }
}
