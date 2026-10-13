package com.bobroflza.tieredarcana.item;

import com.bobroflza.tieredarcana.mana.ArcanePlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A rechargeable mana battery. Right-click to draw mana; recharge it on an active shrine pedestal. */
public class ManaGemItem extends Item {
    public static final String TAG = "StoredMana";
    private final int capacity;

    public ManaGemItem(Properties props, int capacity) {
        super(props.stacksTo(1));
        this.capacity = capacity;
    }

    public int getCapacity() { return capacity; }

    public static int getStored(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt(TAG) : 0;
    }

    public static void setStored(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(TAG, Math.max(0, value));
    }

    /** @return how much was actually added */
    public int charge(ItemStack stack, int amount) {
        int stored = getStored(stack);
        int add = Math.min(amount, capacity - stored);
        if (add > 0) setStored(stack, stored + add);
        return Math.max(add, 0);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) return InteractionResultHolder.success(stack);
        ArcanePlayerData d = sp.getCapability(ArcanePlayerData.CAP).orElse(null);
        if (d == null) return InteractionResultHolder.fail(stack);
        int stored = getStored(stack);
        int need = (int) Math.ceil(d.getMaxMana() - d.getMana());
        int take = Math.min(stored, need);
        if (take <= 0) {
            sp.displayClientMessage(Component.translatable(stored <= 0 ? "message.tieredarcana.gem_empty" : "message.tieredarcana.mana_full")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }
        d.addMana(take);
        setStored(stack, stored - take);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1f, 1.8f);
        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) { return true; }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13f * getStored(stack) / capacity);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.color(0.35f, 0.75f, 1f);
    }

    @Override
    public boolean isFoil(ItemStack stack) { return getStored(stack) >= capacity; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tieredarcana.mana_gem.stored", getStored(stack), capacity).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.tieredarcana.mana_gem.use").withStyle(ChatFormatting.GRAY));
    }
}
