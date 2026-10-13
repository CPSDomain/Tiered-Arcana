package com.bobroflza.tieredarcana.item;

import com.bobroflza.tieredarcana.config.ArcanaConfig;
import com.bobroflza.tieredarcana.mana.ArcanePlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Consumable: permanently raises max mana. Limited per shrine tier the player has attuned to. */
public class ManaCrystalItem extends Item {
    public ManaCrystalItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer sp)) return InteractionResultHolder.success(stack);
        ArcanePlayerData d = sp.getCapability(ArcanePlayerData.CAP).orElse(null);
        if (d == null) return InteractionResultHolder.fail(stack);

        int cap = d.getShrineTier() * ArcanaConfig.CRYSTALS_PER_SHRINE_TIER.get();
        if (d.getShrineTier() == 0) {
            sp.displayClientMessage(Component.translatable("message.tieredarcana.crystal_attune").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        if (d.getCrystalsUsed() >= cap) {
            sp.displayClientMessage(Component.translatable("message.tieredarcana.crystal_cap", cap).withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        d.addCrystal();
        d.addMana(ArcanaConfig.MANA_PER_CRYSTAL.get());
        if (!player.getAbilities().instabuild) stack.shrink(1);
        level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, 1f, 1.2f);
        sp.displayClientMessage(Component.translatable("message.tieredarcana.crystal_used", d.getMaxMana())
                .withStyle(ChatFormatting.AQUA), true);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tieredarcana.mana_crystal").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.tieredarcana.mana_crystal.cap").withStyle(ChatFormatting.GRAY));
    }
}
