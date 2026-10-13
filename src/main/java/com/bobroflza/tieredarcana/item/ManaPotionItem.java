package com.bobroflza.tieredarcana.item;

import com.bobroflza.tieredarcana.mana.ArcanePlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ManaPotionItem extends Item {
    private final int restore;

    public ManaPotionItem(Properties props, int restore) {
        super(props.stacksTo(16));
        this.restore = restore;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (entity instanceof ServerPlayer sp) {
            CriteriaTriggers.CONSUME_ITEM.trigger(sp, stack);
            sp.awardStat(Stats.ITEM_USED.get(this));
            sp.getCapability(ArcanePlayerData.CAP).ifPresent(d -> d.addMana(restore));
        }
        if (entity instanceof Player p && !p.getAbilities().instabuild) {
            stack.shrink(1);
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (stack.isEmpty()) return bottle;
            if (!p.getInventory().add(bottle)) p.drop(bottle, false);
        }
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack) { return 24; }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.DRINK; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public boolean isFoil(ItemStack stack) { return restore >= 100; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tieredarcana.mana_potion", restore).withStyle(ChatFormatting.AQUA));
    }
}
