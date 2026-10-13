package com.bobroflza.tieredarcana.item;

import com.bobroflza.tieredarcana.spell.Spell;
import com.bobroflza.tieredarcana.spell.SpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A scroll holding one spell (NBT "Spell"). Learned by placing it on the shrine pedestal. */
public class SpellScrollItem extends Item {
    public static final String TAG = "Spell";

    public SpellScrollItem(Properties props) {
        super(props.stacksTo(16));
    }

    public static ItemStack of(Item scrollItem, Spell spell) {
        ItemStack stack = new ItemStack(scrollItem);
        stack.getOrCreateTag().putString(TAG, spell.getId().toString());
        return stack;
    }

    @Nullable
    public static Spell getSpell(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains(TAG)) return null;
        return SpellRegistry.get(ResourceLocation.tryParse(stack.getTag().getString(TAG)));
    }

    @Override
    public Component getName(ItemStack stack) {
        Spell s = getSpell(stack);
        if (s == null) return super.getName(stack);
        return Component.translatable("item.tieredarcana.spell_scroll.named", s.getName());
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        Spell s = getSpell(stack);
        if (s == null) return Rarity.COMMON;
        return switch (s.getTier()) {
            case 1 -> Rarity.COMMON;
            case 2 -> Rarity.UNCOMMON;
            case 3 -> Rarity.RARE;
            default -> Rarity.EPIC;
        };
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Spell s = getSpell(stack);
        if (s == null) {
            tooltip.add(Component.translatable("tooltip.tieredarcana.scroll.blank").withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable("tooltip.tieredarcana.scroll.info", s.getSchool().getName(), s.getTier())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(s.getDescription().copy().withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.tieredarcana.scroll.learn").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
