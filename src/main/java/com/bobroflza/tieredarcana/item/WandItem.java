package com.bobroflza.tieredarcana.item;

import com.bobroflza.tieredarcana.spell.SpellCasting;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Casts the spell selected in the spellbook. Higher wand tiers can cast higher-tier spells,
 * spend less mana and hit harder.
 */
public class WandItem extends Item {
    private final int wandTier;
    private final int maxSpellTier;
    private final float costMultiplier;
    private final float power;

    public WandItem(Properties props, int wandTier, int maxSpellTier, float costMultiplier, float power) {
        super(props.stacksTo(1));
        this.wandTier = wandTier;
        this.maxSpellTier = maxSpellTier;
        this.costMultiplier = costMultiplier;
        this.power = power;
    }

    public int getWandTier() { return wandTier; }
    public int getMaxSpellTier() { return maxSpellTier; }
    public float getCostMultiplier() { return costMultiplier; }
    public float getPower() { return power; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            SpellCasting.tryCast(sp, this);
            // small global delay so holding right-click doesn't spam failure messages
            player.getCooldowns().addCooldown(this, 5);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tieredarcana.wand.tier", maxSpellTier).withStyle(ChatFormatting.LIGHT_PURPLE));
        int saving = Math.round((1 - costMultiplier) * 100);
        if (saving > 0) tooltip.add(Component.translatable("tooltip.tieredarcana.wand.cost", saving).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.tieredarcana.wand.power", Math.round(power * 100)).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.tieredarcana.wand.use").withStyle(ChatFormatting.GRAY));
    }
}
